package com.example.recipe

import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.recipe.model.Recipe
import kotlinx.android.synthetic.main.activity_view_recipe.*
import kotlinx.android.synthetic.main.activity_view_recipe.ivRecipe

class ViewRecipeActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var recipeId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_recipe)

        val getIntent: Intent = getIntent()
        recipeId = getIntent.getIntExtra(MainActivity.VIEW_RECIPE_ID,0).toString()
        updateScreen()
    btnEditRecipe.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            btnEditRecipe.id -> {
                val intent = Intent(this, AddOrUpdateRecipeActivity::class.java)
                intent.putExtra(MainActivity.UPDATE_RECIPE_ACTION, recipeId)
                startActivity(intent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MainActivity.returnFromActivity = true
    }

    fun updateScreen(){
        val recipeDetails: ArrayList<Recipe> = arrayListOf()
        val cursor: Cursor = MainActivity.sqLiteHelper.getData("SELECT * FROM "+ getString(R.string.db_name)
                + " WHERE Id = '" + recipeId + "'")

        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val recipeType = cursor.getString(1)
            val recipeName = cursor.getString(2)
            val ingredients = cursor.getString(3)
            val steps = cursor.getString(4)
            val image = cursor.getBlob(5)
            recipeDetails.add(
                Recipe(
                    id,
                    recipeType,
                    recipeName,
                    ingredients,
                    steps,
                    image
                )
            )
        }

        val recipeImage: ByteArray? = recipeDetails[0].image
        val bitmap = BitmapFactory.decodeByteArray(recipeImage, 0, recipeImage!!.size)
        ivRecipe.setImageBitmap(bitmap)
        tvRecipeName.setText(recipeDetails[0].recipeName)
        tvIngredientsDetails.setText(recipeDetails[0].ingredients)
        tvStepsDetails.setText(recipeDetails[0].steps)
    }

    override fun onResume() {
        super.onResume()
        updateScreen()
    }
}