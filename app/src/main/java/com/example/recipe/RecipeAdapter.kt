package com.example.recipe

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.recipe.model.Recipe
import kotlinx.android.synthetic.main.recyclerview_item_recipe.view.*

class RecipeAdapter(val items: List<Recipe>, val onRecipeListener: OnRecipeListener, val onLongClickRecipeListener: OnLongClickRecipeListener) :
    RecyclerView.Adapter<RecipeAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_recipe, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initialize(items.get(position), onRecipeListener, onLongClickRecipeListener)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val imageRecipe = itemView.image_recipe
        val recipeName = itemView.text_name

        fun initialize(item: Recipe, onRecipeClickListener:OnRecipeListener, onLongClickRecipeListener: OnLongClickRecipeListener){
            val recipeImage: ByteArray? = item.image
            val bitmap = BitmapFactory.decodeByteArray(recipeImage, 0, recipeImage!!.size)
            imageRecipe.setImageBitmap(bitmap)
            recipeName.text = item.recipeName

            itemView.setOnClickListener{
                onRecipeClickListener.onRecipeClick(item, adapterPosition)
            }

            itemView.setOnLongClickListener{
                onLongClickRecipeListener.onLongClickRecipe(item, adapterPosition)
                return@setOnLongClickListener true
            }

        }
    }

    interface OnRecipeListener {
        fun onRecipeClick(item: Recipe, position: Int)
    }

    interface OnLongClickRecipeListener {
        fun onLongClickRecipe(item: Recipe, position: Int)
    }
}