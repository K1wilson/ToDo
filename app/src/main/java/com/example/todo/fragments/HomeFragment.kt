package com.example.todo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.databinding.FragmentHomeBinding
import com.example.todo.utils.ToDoAdapter
import com.example.todo.utils.ToDoData
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class HomeFragment : Fragment(), AddToDoFragment.DialogNextBtnClickListener,
    ToDoAdapter.ToDoAdapterClicksInterface {
    private lateinit var auth : FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var navController: NavController
    private lateinit var binding: FragmentHomeBinding
    private  var popFragment: AddToDoFragment?=null
    private lateinit var adapter : ToDoAdapter
    private lateinit var mutableList: MutableList<ToDoData>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        getDataFromFirebase()
        registerEvents()
    }




    private fun init(view: View) {
        navController =Navigation.findNavController(view)
        auth =      FirebaseAuth.getInstance()
        //nxt we will try to fetch data from firebase of a certain user
        databaseRef = FirebaseDatabase.getInstance().reference.
        child("Tasks").child(auth.currentUser?.uid.toString())
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        mutableList = mutableListOf()
        adapter = ToDoAdapter(mutableList)
        adapter.setListener(this)
        binding.recyclerView.adapter = adapter

    }


    private fun registerEvents() {
        binding.addBtnHome.setOnClickListener {
            if (popFragment != null)childFragmentManager.beginTransaction().remove(popFragment!!).commit()
            popFragment = AddToDoFragment()
            popFragment!!.setListener(this)
            popFragment!!.show(
                childFragmentManager,AddToDoFragment.TAG
            )
        }
    }


    private fun getDataFromFirebase() {
        databaseRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               mutableList.clear()
                for (taskSnapshot in snapshot.children){
                    val todoTask = taskSnapshot.key?.let{
                        ToDoData(it,taskSnapshot.value.toString())
                    }

                    if (todoTask != null){
                        mutableList.add(todoTask)
                    }

                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,error.message,Toast.LENGTH_SHORT).show()
            }
        })

    }



    override fun onSaveTask(todo: String, todoET: TextInputEditText) {
        databaseRef.push().setValue(todo).addOnCompleteListener{
            if(
                it.isSuccessful
            ){
                Toast.makeText(context,"Todo saved successfully !!",Toast.LENGTH_SHORT).show()
                todoET.text= null
            }else{
                Toast.makeText(context,it.exception?.message,Toast.LENGTH_SHORT).show()
            }
            popFragment!!.dismiss()
        }
    }

    override fun onUpdateTask(toDoData: ToDoData, todoET: TextInputEditText) {
        //firebase functionality only works with map
        val  map = HashMap <String , Any>()
        map[toDoData.taskId] = toDoData.task
        databaseRef.updateChildren(map).addOnCompleteListener{
            if (it.isSuccessful){
                Toast.makeText(context,"Updated Successfully",Toast.LENGTH_SHORT).show()

            }else{
                Toast.makeText(context,it.exception?.message,Toast.LENGTH_SHORT).show()
            }
            todoET.text = null
            popFragment!!.dismiss()
        }
    }

    override fun onDeleteTaskBtnClicked(toDoData: ToDoData) {
        databaseRef.child(toDoData.taskId).removeValue().addOnCompleteListener{
            if (it.isSuccessful){
                Toast.makeText(context,"Deleted Successfully",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context,it.exception?.message,Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onEditTaskBtnClicked(toDoData: ToDoData) {
        if (popFragment!= null)
            childFragmentManager.beginTransaction().remove(popFragment!!).commit()

        popFragment = AddToDoFragment.newInstance(toDoData.taskId, toDoData.task)

        popFragment!!.setListener(this)
        popFragment!!.show(childFragmentManager, AddToDoFragment.TAG)
    }


}


