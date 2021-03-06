package com.example.inomtest.fragment

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.inomtest.dataClass.ProductResult
import com.example.inomtest.dataClass.room.OnDeleteListener
import com.example.inomtest.dataClass.room.RecentSearchDAO
import com.example.inomtest.dataClass.room.RecentSearchDatabase
import com.example.inomtest.dataClass.room.RecentSearchEntity
import com.example.inomtest.databinding.FragmentSearchBinding
import com.example.inomtest.network.RetrofitManager
import com.example.inomtest.recyclerview.RecentSearchAdapter
import com.example.inomtest.recyclerview.RecyclerItemAdapter
import com.example.inomtest.recyclerview.SearchResultAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SearchFragment : AppCompatActivity(), OnDeleteListener {
    private lateinit var binding: FragmentSearchBinding
    lateinit var navController: NavController
    private lateinit var searchAdapter: RecentSearchAdapter
    private lateinit var recylerAdapter: SearchResultAdapter
    private lateinit var db : RecentSearchDatabase
    private lateinit var searchDAO: RecentSearchDAO
    private val recentWordList = mutableListOf<RecentSearchEntity>()
    private var productList = ArrayList<ProductResult>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSearchBinding.inflate(layoutInflater)

        setContentView(binding.root)
        db = Room.databaseBuilder(this, RecentSearchDatabase::class.java,"room_db")
            .build()
        searchDAO = db.recentSearchDAO()
        searchAdapter = RecentSearchAdapter(this,recentWordList, this)

        refreshAdapter()

        with(binding) {
            RecentWordList.adapter = searchAdapter
            RecentWordList.layoutManager = GridLayoutManager(this@SearchFragment, 2)

            searchBtn.setOnClickListener {
                val content = binding.testEdit.text.toString()
                //????????? api ???????????? ????????????
                val SharedPreferences = getSharedPreferences("access", MODE_PRIVATE)
                val prefEdit = SharedPreferences.edit()
                prefEdit?.putString("searchWord",content)
                prefEdit?.apply()

                if (content.isNotEmpty()) {
                    val recentWord1 = RecentSearchEntity(null, content)
                    insertWord(recentWord1)
                    //?????? api??????
                    RetrofitManager.instance.searchWord(completion = {
                        responseState, responseBody ->
                        productList = responseBody as ArrayList<ProductResult>

                        //api ?????? ??? ????????? ??????
                        when(responseState){
                            RetrofitManager.RESPONSE_STATE.OKAY->{
                                Log.d(TAG,"api ?????? ?????? : ${productList.size}")
                                searchResult.visibility = View.VISIBLE
                                recentHistory.visibility = View.INVISIBLE
                                recylerAdapter = SearchResultAdapter()
                                recylerAdapter.submitList(productList)
                                searchResult.adapter = recylerAdapter
                                searchResult.layoutManager = LinearLayoutManager(this@SearchFragment)
                            }
                            RetrofitManager.RESPONSE_STATE.FAIL->{
                                Toast.makeText(this@SearchFragment, "api ?????? ???????????????.",Toast.LENGTH_SHORT).show()
                                Log.d(TAG,"api ?????? ?????? : $responseBody")
                            }
                        }
                    })
                }
            }
        }


    }
    //room ????????? ???????????? ????????????
    fun refreshAdapter(){
        CoroutineScope(Dispatchers.IO).launch {
            recentWordList.clear()
            recentWordList.addAll(searchDAO.getAll())
            withContext(Dispatchers.Main){
                searchAdapter.notifyDataSetChanged()
            }
        }
    }
    //room ????????? ??????
    fun insertWord(searchWord: RecentSearchEntity){
        CoroutineScope(Dispatchers.IO).launch {
            searchDAO.insert(searchWord)
            refreshAdapter()
        }
    }
    //?????????????????? ?????? _ ????????? ????????? ??????
    override fun onDeleteListener(recentWord: RecentSearchEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            searchDAO.delete(recentWord)
            refreshAdapter()
        }
    }

}