package com.example.recipe


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.recipe.model.Recipe
import kotlinx.android.synthetic.main.activity_add_or_update_recipe.*
import kotlinx.android.synthetic.main.activity_add_or_update_recipe.ivRecipe
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException


class AddOrUpdateRecipeActivity : AppCompatActivity(), View.OnClickListener {
    val REQUEST_CODE_GALLERY = 999
    private lateinit var spinnerValSel: String
    private lateinit var actionOnRecipe: String
    private var updateId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_or_update_recipe)

        setUpSpinner()
        btnAddUpdateRecipe.setOnClickListener(this)
        btnChooseImg.setOnClickListener(this)

        val getIntent: Intent = getIntent()
        val recipeDetails: ArrayList<Recipe> = arrayListOf()
        updateId = getIntent.getStringExtra(MainActivity.UPDATE_RECIPE_ACTION)

        if (TextUtils.isEmpty(updateId)) {
            btnAddUpdateRecipe.text = "Add"
            actionOnRecipe = MainActivity.ADD_RECIPE_ACTION
        } else {
            btnAddUpdateRecipe.text = "Update"
            actionOnRecipe = MainActivity.UPDATE_RECIPE_ACTION
            val cursor: Cursor = MainActivity.sqLiteHelper.getData(
                "SELECT * FROM " + getString(R.string.db_name)
                        + " WHERE Id = '" + updateId + "'"
            )

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

            etRecipeName.setText(recipeDetails[0].recipeName)
            etIngredients.setText(recipeDetails[0].ingredients)
            etSteps.setText(recipeDetails[0].steps)

            //set the default according to Recipe Type
            val spinnerPosition: Int = MainActivity.options.indexOf(recipeDetails[0].recipeType)
            spinnerRecipeType.setSelection(spinnerPosition)
        }
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            btnAddUpdateRecipe.id -> {
                if (actionOnRecipe == MainActivity.ADD_RECIPE_ACTION) {
                    try {
                        if ((!TextUtils.isEmpty(etRecipeName.text.toString()) ||
                                    !TextUtils.isEmpty(etIngredients.text.toString()) ||
                                    !TextUtils.isEmpty(etSteps.text.toString()))
                            && (!TextUtils.isEmpty(spinnerValSel))
                            && (ivRecipe.getDrawable() !== null)) {
                            MainActivity.sqLiteHelper.insertData(
                                spinnerValSel,
                                etRecipeName.text.toString(),
                                etIngredients.text.toString(),
                                etSteps.text.toString(),
                                imageViewToByte(ivRecipe)
                            )
                            Toast.makeText(
                                applicationContext,
                                "Added successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Ensure a recipe type and image, and at least one other information is filled",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else if (actionOnRecipe == MainActivity.UPDATE_RECIPE_ACTION) {
                    if ((!TextUtils.isEmpty(etRecipeName.text.toString()) ||
                        !TextUtils.isEmpty(etIngredients.text.toString()) ||
                        !TextUtils.isEmpty(etSteps.text.toString()))
                        && (!TextUtils.isEmpty(spinnerValSel))
                        && (ivRecipe.getDrawable() !== null)) {
                        MainActivity.sqLiteHelper.updateData(
                            updateId?.toInt(),
                            spinnerValSel,
                            etRecipeName.text.toString(),
                            etIngredients.text.toString(),
                            etSteps.text.toString(),
                            imageViewToByte(ivRecipe)
                        )
                        Toast.makeText(
                            applicationContext,
                            "Updated successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Ensure a recipe type and image, and at least one other information is filled",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            btnChooseImg.id -> {
                ActivityCompat.requestPermissions(
                    this@AddOrUpdateRecipeActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_GALLERY
                )
            }
        }
    }

    fun imageViewToByte(image: ImageView): ByteArray? {
        val bitmap = (image.drawable as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_GALLERY) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_CODE_GALLERY)
            } else {
                Toast.makeText(
                    applicationContext,
                    "You don't have permission to access file location!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            try {
                val inputStream = contentResolver.openInputStream(uri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                ivRecipe.setImageBitmap(bitmap)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun setUpSpinner() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.recipe_type_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerRecipeType.adapter = adapter
        }

        spinnerRecipeType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                spinnerValSel = MainActivity.options[p2]
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MainActivity.returnFromActivity = true
    }
// // Still trying to see how I can control the edit text to have bullets
//        editTextTextMultiLine.doOnTextChanged { text, start, before, count ->
//            var text = text
//            if (count > before) {
//                if (text.toString().length == 1) {
//                    text = "• $text"
//                    editTextTextMultiLine.setText(text)
//                    editTextTextMultiLine.setSelection(editTextTextMultiLine.getText()!!.length)
//                }
//                if (text.toString().endsWith("\n")) {
//                    text = text.toString().replace("\n", "\n• ")
//                    text = text.toString().replace("• •", "•")
//                    editTextTextMultiLine.setText(text)
//                    editTextTextMultiLine.setSelection(editTextTextMultiLine.getText()!!.length)
//                }
//            }
//
//        }
}