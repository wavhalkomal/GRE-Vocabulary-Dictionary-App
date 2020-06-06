package com.example.dictionaryactivity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.os.AsyncTask
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {

    companion object{
        val TAG = "MainActivity"
        val LAST_SEARCH_WORD : String = "LAST_STRING_WORD"
    }
    var mDbHelper: DatabaseHelper? = null
    var mSearchListAdapter: SearchListAdapter? = null
    var mSearchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mSearchQuery = savedInstanceState?.getString(LAST_SEARCH_WORD )?: ""
        mDbHelper = DatabaseHelper(applicationContext)

        if(!isDbLoaded()){
            setContentView(R.layout.activity_dictionary_loading)
            LoadViewTask(this).execute()
        }else {
            showDictUI()
        }
    }

    private fun showDictUI() {
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setIcon(R.mipmap.ic_launcher)
        //Testing - adding words into dict database
        //        val dbHelper = DatabaseHelper(applicationContext)
        //        dbHelper.addSomeDummyWord()
        //        dbHelper.getWords()
        mSearchListAdapter = SearchListAdapter(applicationContext, mDbHelper!!.getWords(mSearchQuery))
        val lstWords = (findViewById<ListView>(R.id.lstWords))
        lstWords.adapter = mSearchListAdapter
        lstWords.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, id ->
            val wordDetailIntent = Intent(applicationContext, WordDetailActivity::class.java)
            wordDetailIntent.putExtra(WordDetailActivity.WORD_ID, "$id")
            startActivity(wordDetailIntent)
        }
    }

    private fun isDbLoaded(): Boolean{
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPref.getBoolean(DatabaseHelper.DB_CREATED, false)
    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//
//        outState?.getString(LAST_SEARCH_WORD, mSearchQuery)
//    }
//
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        mSearchQuery = savedInstanceState?.getString(LAST_SEARCH_WORD )?: ""
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if(intent?.action.equals(Intent.ACTION_SEARCH)) {
            val searchQuery = intent?.getStringExtra(SearchManager.QUERY) ?: ""
            updateListByQuery(searchQuery)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchView: SearchView? = menu.findItem(R.id.action_search).actionView as? SearchView
        val searchManager: SearchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                updateListByQuery(newText ?: "")
                return true
            }
        })
        return true
    }

    private fun updateListByQuery(searchQuery: String) {
        mSearchQuery = searchQuery
        mSearchListAdapter?.changeCursor(mDbHelper!!.getWords(searchQuery))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //handle action bar item click here . the action bar will
        //automatically handle click on the home / up button , so long
        //as you specify a parent activity in androidmanfist.xml
        return when (item.itemId) {
            R.id.action_search -> {
                onSearchRequested()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class LoadViewTask(activity: MainActivity) : AsyncTask<Void, Void, Void>() {
            private var mActivity = WeakReference<MainActivity>(activity)

            override fun doInBackground(vararg params: Void?): Void? {
                if(getActivityInstance()?.mDbHelper?.readableDatabase?.isOpen == true) {
                    Log.d(TAG, "Database is OK.")
                }
                return null
            }

            override fun onPostExecute(result: Void?) {
                if(getActivityInstance()?.isDbLoaded() == true) {
                    getActivityInstance()?.showDictUI()
                }
            }
            private fun getActivityInstance() = mActivity.get()
        }
}
