package com.removedust.speaker.cleaner.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class AbsBaseFragment<V : ViewDataBinding?> : Fragment() {
    protected var binding: V? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (binding == null) {
            binding = DataBindingUtil.inflate<V?>(inflater, this.layout, null, false)
            binding!!.setLifecycleOwner(this)
        }
        return binding!!.getRoot()
    }

    abstract val layout: Int
    abstract fun initView()
}
