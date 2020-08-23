package com.example.recipe

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipe.model.Recipe
import com.example.recipe.util.SQLiteHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener, RecipeAdapter.OnRecipeListener, RecipeAdapter.OnLongClickRecipeListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var recipeData: ArrayList<Recipe>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recipeData = arrayListOf<Recipe>()
        viewManager = LinearLayoutManager(this)
        viewAdapter = RecipeAdapter(recipeData, this, this)
        recyclerView = findViewById<RecyclerView>(R.id.recycler_recipe_list).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager
            addItemDecoration(DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL))
            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        sqLiteHelper = SQLiteHelper(
            this,
            "RecipeDB.sqlite",
            null,
            1
        )
        sqLiteHelper.queryData("CREATE TABLE IF NOT EXISTS "
                + getString(R.string.db_name) +
                " (Id INTEGER PRIMARY KEY AUTOINCREMENT, recipeType VARCHAR, recipeName VARCHAR, ingredients VARCHAR, steps VARCHAR, image BLOB)")

        setUpSpinner()
        fabAdd.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            fabAdd.id -> {
                val intent = Intent(applicationContext, AddOrUpdateRecipeActivity::class.java)
                startActivity(intent)
            }
        }
    }


    fun setUpSpinner(){
        options = resources.getStringArray(R.array.recipe_type_array)

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.recipe_type_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
            spinner.setSelection(lastSelArray)
        }

        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                // get data from sqlite depending on item selected

                getDataAndNotifyAdapter(p2)
            }
        }
    }

    fun getDataAndNotifyAdapter(lastSelArray: Int){
        val cursor: Cursor = sqLiteHelper.getData("SELECT Id, recipeName, image, recipeType FROM " +
                "${getString(R.string.db_name)}" +
                if (lastSelArray == 0) "" else " WHERE recipeType = '${options[lastSelArray]}'"
        )

        recipeData.clear()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val recipeName = cursor.getString(1)
            val image = cursor.getBlob(2)
            val recipeType: String = cursor.getString(3)
            recipeData.add(
                Recipe(
                    id,
                    recipeType,
                    recipeName,
                    null,
                    null,
                    image
                )
            )
        }
        viewAdapter.notifyDataSetChanged()
    }

    override fun onRecipeClick(item: Recipe, position: Int) {
        val intent = Intent(this, ViewRecipeActivity::class.java)
        intent.putExtra(VIEW_RECIPE_ID, item.id)
        startActivity(intent)
    }

    override fun onLongClickRecipe(item: Recipe, position: Int) {
        // setup the alert builder
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an option")

        // add a list
        val dialogOptions = arrayOf("Update", "Delete")
        builder.setItems(dialogOptions) { dialog, which ->
            when (which) {
                0 -> {
                    val intent = Intent(this, AddOrUpdateRecipeActivity::class.java)
                    intent.putExtra(UPDATE_RECIPE_ACTION, item.id.toString())
                    startActivity(intent)
                }
                1 -> {
                    item.id?.let { sqLiteHelper.deleteData(it) }
                    getDataAndNotifyAdapter(lastSelArray)
                }
            }
        }

// create and show the alert dialog
        val dialog = builder.create()
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        getDataAndNotifyAdapter(lastSelArray)
    }

    override fun onPause() {
        super.onPause()
        lastSelArray = spinner.getSelectedItemPosition()
    }

    companion object {
        lateinit var sqLiteHelper: SQLiteHelper
        private var lastSelArray: Int = 0
        var options: Array<String> = emptyArray()
        val VIEW_RECIPE_ID = "VIEW RECIPE ID"
        val ADD_RECIPE_ACTION = "ADD RECIPE ACTION"
        val UPDATE_RECIPE_ACTION = "UPDATE RECIPE ACTION"
        var returnFromActivity:Boolean = false
    }
}