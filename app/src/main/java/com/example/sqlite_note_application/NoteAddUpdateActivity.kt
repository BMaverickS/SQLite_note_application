package com.example.sqlite_note_application

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.sqlite_note_application.databinding.ActivityNoteAddUpdateBinding
import db.DatabaseContract
import db.DatabaseContract.NoteColumns.Companion.DATE
import db.NoteHelper
import entity.Note
import java.text.SimpleDateFormat
import java.util.*

class NoteAddUpdateActivity : AppCompatActivity(), View.OnClickListener {

    private var isEdit = false
    private var note: Note? = null
    private var position: Int = 0
    private lateinit var noteHelper: NoteHelper

    private lateinit var binding: ActivityNoteAddUpdateBinding

    companion object
    {
        const val EXTRA_NOTE = "extra_note"
        const val EXTRA_POSITION = "extra_position"
        const val REQUEST_ADD = 100
        const val RESULT_ADD = 101
        const val REQUEST_UPDATE = 200
        const val RESULT_UPDATE = 201
        const val RESULT_DELETE = 301
        const val ALERT_DIALOG_CLOSE = 10
        const val ALERT_DIALOG_DELETE = 20
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_note_add_update)

        binding = ActivityNoteAddUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        noteHelper = NoteHelper.getInstance(applicationContext)
        noteHelper.open()

        note = intent.getParcelableExtra(EXTRA_NOTE)
        if (note != null)
        {
            position = intent.getIntExtra(EXTRA_POSITION, 0)
            isEdit = true
        }
        else
        {
            note = Note()
        }

        val actionBarTitle : String
        val btnTitle : String

        if (isEdit)
        {
            actionBarTitle = "Change"
            btnTitle = "Update"

            note?.let {
                binding.edtTitle.setText(it.title)
                binding.edtDescription.setText(it.description)
            }
        }
        else
        {
            actionBarTitle = "Add"
            btnTitle = "Save"
        }

        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSubmit.text = btnTitle

        binding.btnSubmit.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (view.id == R.id.btn_submit)
        {
            val title = binding.edtTitle.text.toString().trim()
            val description = binding.edtDescription.text.toString().trim()

            if (title.isEmpty())
            {
                binding.edtTitle.error = "Field can not be blank"
                return
            }

            note?.title = title
            note?.description = description

            val intent = Intent()
            intent.putExtra(EXTRA_NOTE, note)
            intent.putExtra(EXTRA_POSITION, position)

            val values = ContentValues()
            values.put(DatabaseContract.NoteColumns.TITLE, title)
            values.put(DatabaseContract.NoteColumns.DESCRIPTION, description)

            // Variable isEdit akan menjadi true pada saat Intent melalui kelas adapter, karena
            // mengirimkan objek listnotes. Lalu pada NoteAddUpdateActivity akan divalidasi. Jika
            // tidak null maka isEdit akan berubah true.
            if (isEdit)     // update
            {
                val result = noteHelper.update(note?.id.toString(), values).toLong()
                if (result > 0)
                {
                    setResult(RESULT_UPDATE, intent)
                    finish()
                }
                else
                {
                    Toast.makeText(this@NoteAddUpdateActivity, "Failed to update data",
                        Toast.LENGTH_SHORT).show()
                }
            }
            else        // add
            {
                note?.date = getCurrentDate()
                values.put(DATE, getCurrentDate())
                val result = noteHelper.insert(values)

                if (result > 0)
                {
                    note?.id = result.toInt()
                    setResult(RESULT_ADD, intent)
                    finish()
                }
                else
                {
                    Toast.makeText(this@NoteAddUpdateActivity, "Failed to add data",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getCurrentDate() : String
    {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val date = Date()

        return dateFormat.format(date)
    }

    /*Ketika pengguna berada pada proses pembaruan data, setiap kolom pada form sudah terisi
    otomatis. Ikon untuk hapus di sudut kanan atas ActionBar berfungsi untuk menghapus data.
    Kode berikut akan menjalankan kebutuhan di atas. Intinya cek nilai boolean isEdit yang
    berasal dari proses validasi, apakah objek note berisi null atau tidak.*/
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isEdit) {
            menuInflater.inflate(R.menu.menu_form, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> showAlertDialog(ALERT_DIALOG_DELETE)
            android.R.id.home -> showAlertDialog(ALERT_DIALOG_CLOSE)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        showAlertDialog(ALERT_DIALOG_CLOSE)
    }

    private fun showAlertDialog(type : Int)
    {
        val isDialogClose = type == ALERT_DIALOG_CLOSE
        val dialogTitle: String
        val dialogMessage: String

        if (isDialogClose)
        {
            dialogTitle = "Canceled"
            dialogMessage = "Do you want to cancel change on form ?"
        }
        else
        {
            dialogMessage = "Are you sure to delete this item ?"
            dialogTitle = "Delete Note"
        }

        // use AlertDialog to make confirmation dialog, show when back button is pressed
        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder.setTitle(dialogTitle)    // set dialog according to usage
        alertDialogBuilder.setMessage(dialogMessage).setCancelable(false) // is it to show
            .setPositiveButton("Yes") { _, _ ->               // close dialog or delete data
                if (isDialogClose)
                {
                    finish()
                }
                else
                {
                    val result = noteHelper.deleteById(note?.id.toString()).toLong()
                    if (result > 0)
                    {
                     // every action will send data and RESULT_CODE to be processed on MainActivity
                        val intent = Intent()
                        intent.putExtra(EXTRA_POSITION, position)
                        setResult(RESULT_DELETE, intent)
                        finish()
                    }
                    else
                    {
                        Toast.makeText(this@NoteAddUpdateActivity, "Failed to delete"
                                + " data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Tidak") { dialog, _ -> dialog.cancel() }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}

/* Tanggung jawab utama NoteAddUpdateActivity adalah sebagai berikut:
- Menyediakan form untuk melakukan proses input data.

- Menyediakan form untuk melakukan proses pembaruan data.

- Jika pengguna berada pada proses pembaruan data maka setiap kolom pada form sudah terisi otomatis
 dan ikon untuk hapus yang berada pada sudut kanan atas ActionBar ditampilkan dan berfungsi untuk
 menghapus data.

- Sebelum proses penghapusan data, dialog konfirmasi akan tampil. Pengguna akan ditanya terkait
penghapusan yang akan dilakukan.

- Jika pengguna menekan tombol back (kembali) baik pada ActionBar maupun peranti, maka akan tampil
dialog konfirmasi sebelum menutup halaman.

- Masih ingat materi di mana sebuah Activity menjalankan Activity lain dan menerima nilai balik pada
 metode onActivityResult()? Tepatnya di Activity yang dijalankan dan ditutup dengan menggunakan
 parameter REQUEST dan RESULTCODE. Jika Anda lupa, baca kembali modul 1 tentang Intent ya!
*/