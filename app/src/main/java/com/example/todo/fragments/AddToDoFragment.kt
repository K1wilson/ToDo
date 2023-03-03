package com.example.todo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.todo.databinding.FragmentAddToDoBinding
import com.example.todo.utils.ToDoData
import com.google.android.material.textfield.TextInputEditText


class AddToDoFragment : DialogFragment() {

    private lateinit var binding  : FragmentAddToDoBinding
    private lateinit var listener: DialogNextBtnClickListener
    private var todoData : ToDoData? = null


    fun setListener(listener: HomeFragment){
        this.listener = listener
    }
    companion object{
        const val TAG = "AddToDoFragment"

        @JvmStatic
        fun newInstance(taskId:String , task :String)= AddToDoFragment().apply {
            arguments= Bundle().apply {
                putString("taskId",taskId)
                putString("task", task)
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAddToDoBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments!= null) {
        todoData = ToDoData(arguments?.getString("taskId").toString(),
            arguments?.getString("task").toString())

            binding.todoEt.setText((todoData?.task))
        }

        registerEvents()
    }

    private fun registerEvents() {
        binding.todoNextBtn.setOnClickListener {
            val todoTask = binding.todoEt.text.toString()
            if(todoTask.isNotEmpty()){
                if (todoData==null){
                    listener.onSaveTask(todoTask,binding.todoEt)
                }else{
                    todoData?.task = todoTask
                    listener.onUpdateTask(todoData!! ,binding.todoEt)
                }

                listener.onSaveTask(todoTask,binding.todoEt)
            }
            else{
                Toast.makeText(context,"Please type some task", Toast.LENGTH_SHORT).show()
            }
        }
        binding.todoClose.setOnClickListener { dismiss() }
    }
    interface DialogNextBtnClickListener{
        fun onSaveTask(todo:String,todoET : TextInputEditText)
        fun onUpdateTask(  toDoData: ToDoData,todoET : TextInputEditText)
    }


}