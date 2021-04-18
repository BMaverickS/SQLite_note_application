package com.example.sqlite_note_application

import adapter.NoteAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sqlite_note_application.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import db.NoteHelper
import entity.Note
import helper.MappingHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {  // error !!!
                                                // MainActivity function :
    private lateinit var adapter: NoteAdapter   // show data from database on Note table ascending,
    // receive return value from each action and process that is being done on NoteAddUpdateActivity
    private lateinit var binding: ActivityMainBinding

    companion object
    {
        private const val EXTRA_STATE = "EXTRA_STATE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Notes"

        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        binding.rvNotes.setHasFixedSize(true)
        adapter = NoteAdapter(this)
        binding.rvNotes.adapter = adapter

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this@MainActivity, NoteAddUpdateActivity::class.java)
            startActivityForResult(intent, NoteAddUpdateActivity.REQUEST_ADD)
        }

        if (savedInstanceState == null)
        {
            // proses ambil data
            loadNotesAsync()
        }
        else
        {
            val list = savedInstanceState.getParcelableArrayList<Note>(EXTRA_STATE)
            if (list != null)
            {
                adapter.listNotes = list
            }
        }
    }

    private fun loadNotesAsync()    // load data from table and show data to list asynchronously
    {                               // by using background process
        GlobalScope.launch(Dispatchers.Main) {
            binding.progressbar.visibility = View.VISIBLE

            // main rules to use and access SQLite :
            // make instance and open connection to method onCreate(), then don't forget to close
            // connection after being used
            val noteHelper = NoteHelper.getInstance(applicationContext)
            noteHelper.open()

            // use async because we wants return value from called function
            val deferredNotes = async(Dispatchers.IO) {
                val cursor = noteHelper.queryAll()
                MappingHelper.mapCursorToArrayList(cursor)
            }

            binding.progressbar.visibility = View.INVISIBLE

            // use await() to get return value
            val notes = deferredNotes.await()

            // close after done
            noteHelper.close()  // call after all process related to database has done

            if (notes.size > 0)
            {
                adapter.listNotes = notes
            }
            else
            {
                adapter.listNotes = ArrayList()
                showSnackbarMessage("Tidak ada data saat ini")
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle)
    {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(EXTRA_STATE, adapter.listNotes)
    }

    // do action after receive return value from all actions done in NoteAddUpdateActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null)
        {
            when (requestCode)
            {
                // Akan dipanggil jika request codenya ADD

                /*Baris di atas akan dijalankan ketika terjadi penambahan data pada
                NoteAddUpdateActivity. Alhasil, ketika metode ini dijalankan maka kita akan
                membuat objek note baru dan inisiasikan dengan getParcelableExtra. Lalu panggil
                metode addItem yang berada di adapter dengan memasukan objek note sebagai argumen.
                Metode tersebut akan menjalankan notifyItemInserted dan penambahan arraylist-nya.
                Lalu objek rvNotes akan melakukan smoothscrolling, dan terakhir muncul notifikasi
                pesan dengan menggunakan Snackbar.*/
                NoteAddUpdateActivity.REQUEST_ADD -> if (resultCode == NoteAddUpdateActivity
                        .RESULT_ADD) {
                    val note = data.getParcelableExtra<Note>(NoteAddUpdateActivity.EXTRA_NOTE)
                            as Note

                    adapter.addItem(note)
                    binding.rvNotes.smoothScrollToPosition(adapter.itemCount - 1)

                    showSnackbarMessage("One item successfully added")
                }

                // Update dan Delete memiliki request code sama akan tetapi result codenya berbeda
                NoteAddUpdateActivity.REQUEST_UPDATE ->
                    when (resultCode) {
                        /*
                        Akan dipanggil jika result codenya  UPDATE
                        Semua data di load kembali dari awal
                        */
                        /*Baris di atas akan dijalankan ketika terjadi perubahan data pada
                        NoteAddUpdateActivity. Prosesnya hampir sama seperti ketika ada penambahan
                        data, tetapi di sini kita harus membuat objek baru yaitu position. Sebabnya,
                        metode updateItem membutuhkan 2 argumen yaitu position dan Note.*/
                        NoteAddUpdateActivity.RESULT_UPDATE -> {
                            val note = data.getParcelableExtra<Note>(NoteAddUpdateActivity.EXTRA_NOTE) as Note
                            val position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION, 0)

                            adapter.updateItem(position, note)

                            binding.rvNotes.smoothScrollToPosition(position)
                            showSnackbarMessage("One item has been successfully changed")
                        }

                        /*
                        Akan dipanggil jika result codenya DELETE
                        Delete akan menghapus data dari list berdasarkan dari position
                        */
                        /*Baris di atas akan dijalankan jika nilai resultCode-nya adalah
                        RESULT_DELETE. Di sini kita hanya membutuhkan position karena metode
                        removeItem hanya membutuhkan position untuk digunakan pada notifyItemRemoved
                        dan penghapusan data pada arraylist-nya.*/
                        NoteAddUpdateActivity.RESULT_DELETE -> {
                            val position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION, 0)
                            adapter.removeItem(position)
                            showSnackbarMessage("One item has been successfully deleted")
                        }
                    }
            }
        }
    }

    private fun showSnackbarMessage(message: String) {
        Snackbar.make(binding.rvNotes, message, Snackbar.LENGTH_SHORT).show()
    }
}

// every actions done in NoteAddUpdateActivity will affects MainActivity, be it for add, update, or
// delete.

// onActivityResult() will receive data from intent that is sent and selected based on requestCode
// and resultCode.