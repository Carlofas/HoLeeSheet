package es.charles.holeesheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import es.charles.holeesheet.databinding.HoLeeSheetMenuLayoutBinding

class HoLeeSheetMenu : BottomSheetDialogFragment() {

    private var _binding: HoLeeSheetMenuLayoutBinding? = null
    private val binding get() = _binding!!

    private var cancelOnTouchOutSide: Boolean = true
    private var cancelable: Boolean = true
    private var onItemClickListener: ((HoLeeMenuItem) -> Unit)? = null
    private var items: List<HoLeeMenuItem> = emptyList()
    private var roundCorners: Boolean = true
    private var showDragIcon: Boolean = true

    private val adaptador by lazy {
        HoLeeSheetMenuAdapter{ item ->
            onItemClickListener?.invoke(item)
            this@HoLeeSheetMenu.dismiss()
        }
    }


    companion object {

        const val TAG = "HoLeeSheetMenu"
        private const val ARG_CANCELABLE = "cancelable"
        private const val ARG_CANCEL_ON_TOUCH_OUT_SIDE = "cancelOnTouchOutSide"
        private const val ARG_ROUND_CORNERS = "roundCorners"
        private const val ARG_SHOW_DRAG_ICON = "showDragIcon"

        @JvmStatic
        private fun newInstance(
            items: List<HoLeeMenuItem>,
            cancelable: Boolean = true,
            cancelOnTouchOutSide: Boolean = true,
            roundCorners: Boolean = true,
            showDragIcon: Boolean = true,
            onItemClickListener: ((HoLeeMenuItem) -> Unit)? = null

        ) = HoLeeSheetMenu().apply {
            this.items = items
            this.onItemClickListener = onItemClickListener

            arguments = Bundle().apply {
                putBoolean(ARG_CANCELABLE, cancelable)
                putBoolean(ARG_CANCEL_ON_TOUCH_OUT_SIDE, cancelOnTouchOutSide)
                putBoolean(ARG_ROUND_CORNERS, roundCorners)
                putBoolean(ARG_SHOW_DRAG_ICON, showDragIcon)
            }
        }

        fun newInstanceBuilder(builder: Builder) =
            newInstance(
                items = builder.items,
                cancelable = builder.cancelable,
                cancelOnTouchOutSide = builder.cancelOnTouchOutSide,
                onItemClickListener = builder.onItemClickListener,
                roundCorners = builder.roundCorners,
                showDragIcon = builder.showDragIcon
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cancelable = it.getBoolean(ARG_CANCELABLE)
            cancelOnTouchOutSide = it.getBoolean(ARG_CANCEL_ON_TOUCH_OUT_SIDE)
            roundCorners = it.getBoolean(ARG_ROUND_CORNERS)
            showDragIcon = it.getBoolean(ARG_SHOW_DRAG_ICON)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = HoLeeSheetMenuLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!roundCorners) binding.hoLeeSheetMenuMainLayout.setBackgroundResource(R.drawable.bg_ho_lee_menu)
        if (!showDragIcon) binding.hoLeeSheetMenuDragIcon.visibility = View.GONE
        initAdapter()
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val rootView = View.inflate(context, R.layout.ho_lee_sheet_menu_layout, null)

            val dialog = BottomSheetDialog(requireContext()/*, theme*/).also { dx ->
                isCancelable = cancelable
                dx.apply {
                    window?.attributes?.windowAnimations = R.style.BottomSheetDxBaseAnimation
                    dismissWithAnimation = true
                    setCanceledOnTouchOutside(cancelOnTouchOutSide)
                    setContentView(rootView)
                    setOnShowListener {
                        val d = it as BottomSheetDialog
                        val bottomSheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                        bottomSheet?.let { view ->
                            val behavior = BottomSheetBehavior.from(view)
                            behavior.state = BottomSheetBehavior.STATE_EXPANDED
                            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                                override fun onStateChanged(bottomSheet: View, newState: Int) {
                                    if (newState == BottomSheetBehavior.STATE_HIDDEN)
                                        this@HoLeeSheetMenu.dismiss()
                                }
                                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                            })
                        }
                    }
                }

            }
            dialog
        }
    }

    private fun initAdapter() = with(binding) {
        rvHoLeeSheetMenu.apply {
            setHasFixedSize(true)
            adapter = adaptador
        }
        adaptador.submitList(items)
    }

    fun show(fragmentManager: FragmentManager) = show(fragmentManager, TAG)


    class Builder {
        var items: List<HoLeeMenuItem> = emptyList()
        var onItemClickListener: ((HoLeeMenuItem) -> Unit)? = null
        var cancelable: Boolean = true
        var cancelOnTouchOutSide: Boolean = true
        var roundCorners: Boolean = true
        var showDragIcon: Boolean = true

        fun build() = newInstanceBuilder(this)

        fun buildAndShow(fragmentManager: FragmentManager) =
            fragmentManager.findFragmentByTag(TAG) ?: run { build().show(fragmentManager) }

        fun setItems(items: List<HoLeeMenuItem>) = apply { this.items = items }
        fun setOnItemClickListener(onItemClickListener: ((HoLeeMenuItem) -> Unit)?) = apply { this.onItemClickListener = onItemClickListener }
        fun setCancelable(cancelable: Boolean) = apply { this.cancelable = cancelable }
        fun setCancelOnTouchOutSide(cancelOnTouchOutSide: Boolean) = apply { this.cancelOnTouchOutSide = cancelOnTouchOutSide }
        fun setRoundCorners(roundCorners: Boolean) = apply { this.roundCorners = roundCorners }
        fun setShowDragIcon(showDragIcon: Boolean) = apply { this.showDragIcon = showDragIcon }
    }
}