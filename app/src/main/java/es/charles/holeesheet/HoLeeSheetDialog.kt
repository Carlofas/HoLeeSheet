package es.charles.holeesheet

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.RawRes
import androidx.annotation.StyleRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeView
import es.charles.holeesheet.databinding.HoLeeSheetLayoutBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collectLatest

open class HoLeeSheetDialog : BottomSheetDialogFragment() {

    private var _binding : HoLeeSheetLayoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HoLeeSheetViewModel by viewModels()

    // CAMPOS COMUNES
    private var typeLayout: TypeLayout? = null
    private var icon: Int? = null
    private var title: String? = null
    private var message: CharSequence? = null
    private var cancelOnTouchOutSide: Boolean = true
    private var cancel: Boolean = true
    private var controlDismiss: Boolean = false
    private var customTheme: Int? = null
    private var job: Job? = null

    // CAMPOS PARA LOTTIE / IMAGEN
    private var lottieFile: Int? = null
    private var lottieLoop: Boolean = true
    private var imagenFile: Int? = null
    private var bitmap: Bitmap? = null


    //CAMPOS PARA INPUT
    private var inputType: Int? = null
    private var textHint: String? = null
    private var endIconClearText: Boolean = false
    private var imeOptions: Int? = null
    private var inputLayout: TextInputLayout? = null
    private var textoInput: String? = null
    private var errorMessage: String? = null

    private var dropdownItems: List<String>? = null
    private var initialValuePicker: Int? = null
    private var maxValue: Int? = null
    private var minValue: Int? = null

    //LISTENERS
    private var onPositiveClickButton: ((HoLeeSheetDialog) -> Unit)? = null
    private var onNegativeClickButton: ((HoLeeSheetDialog) -> Unit)? = null
    private var inputListener : ((HoLeeSheetDialog, String) -> Unit)? = null
    private var onCancelListener : (() -> Unit)? = null
    private var onPickerNumberListener : ((HoLeeSheetDialog, Int) -> Unit)? = null
    private var onSelectorListener: ((HoLeeSheetDialog, Int?) -> Unit)? = null
    private var barcodeListener: ((HoLeeSheetDialog, String) -> Unit)? = null

    private var positiveTextButton: String? = null
    private var negativeTextButton: String? = null

    private var customLayout: Int? = null

    private var viewStubCallBack : ((ViewStub, HoLeeSheetDialog) -> Unit)? = null

    private var barcodeView: BarcodeView? = null
    private var barcodeContainer : MaterialCardView? = null


    companion object {
        const val TAG = "HoLeeSheetDialog"
        private const val ARG_ICON = "argHoLeeSheetIcon"
        private const val ARG_TITLE = "argHoLeeSheetTitle"
        private const val ARG_MESSAGE = "argHoLeeSheetMessage"
        private const val ARG_CANCEL_ON_TOUCH_OUTSIDE = "argHoLeeSheetCancelOnTouchOutSide"
        private const val ARG_CONTROL_DISMISS = "argHoLeeSheetControlDismiss"
        private const val ARG_CANCELABLE = "argHoLeeSheetTcancelable"
        private const val ARG_THEME = "argHoLeeSheetTheme"
        private const val ARG_LOTTIE_FILE = "argHoLeeSheetLottieFile"
        private const val ARG_LOTTIE_LOOP = "argHoLeeSheetLottieLoop"
        private const val ARG_IMAGEN_FILE = "argHoLeeSheetImagenFile"
        private const val ARG_POSITIVE_TEXT_BUTTON = "argHoLeeSheetPositiveTextButton"
        private const val ARG_NEGATIVE_TEXT_BUTTON = "argHoLeeSheetNegativeTextButton"
        private const val ARG_INPUT_TYPE = "argHoLeeSheetInputType"
        private const val ARG_INPUT_HINT = "argHoLeeSheetInputHint"
        private const val ARG_END_ICON_CLEAR_TEXT = "argHoLeeSheetEndIconClearText"
        private const val ARG_IME_OPTIONS = "argHoLeeSheetImeOptions"
        private const val ARG_TEXTO_INPUT = "argHoLeeSheetTextInput"
        private const val ARG_TYPE_LAYOUT = "argHoLeeSheetTypeLayout"
        private const val ARG_CUSTOM_LAYOUT = "argHoLeeSheetCustomLayout"
        private const val ARG_ERROR_MESSAGE = "argHoLeeSheetErrorMessage"
        private const val ARG_BITMAP = "argHoLeeSheetBitmap"
        private const val ARG_INITIAL_VALUE_PICKER = "argHoLeeSheetInitialValuePicker"
        private const val ARG_MIN_VALUE = "argHoLeeSheetMinValue"
        private const val ARG_MAX_VALUE = "argHoLeeSheetMaxValue"

        @JvmStatic
        private fun newInstance(
            @DrawableRes icon: Int? = null,
            title: String,
            message: CharSequence? = null,
            cancelOnTouchOutSide: Boolean = true,
            cancelable: Boolean = true,
            theme: Int? = null,

            @RawRes lottieFile: Int? = null,
            lottieLoop: Boolean = true,
            @DrawableRes imagenFile: Int? = null,
            bitmap: Bitmap? = null,

            positiveTextButton: String? = null,
            negativeTextButton: String? = null,
            controlDismiss: Boolean = false,
            inputType: Int? = null,

            textInput: String? = null,
            textHint : String? = null,
            endIconClearText : Boolean = true,
            imeOptions: Int? = null,
            typeLayout: TypeLayout? = null,
            errorMessage: String? = null,
            initialValuePicker: Int? = null,
            maxValue: Int? = null,
            minValue: Int? = null,

            onCancelListener: (() -> Unit)? = null,
            onPositiveClickButton: ((HoLeeSheetDialog) -> Unit)? = null,
            onNegativeClickButton: ((HoLeeSheetDialog) -> Unit)? = null,
            inputListener: ((HoLeeSheetDialog, String) -> Unit)? = null,
            onPickerNumberListener : ((HoLeeSheetDialog, Int) -> Unit)? = null,
            onSelectorListener: ((HoLeeSheetDialog, Int?) -> Unit)? = null,
            dropdownItems: List<String>? = null,
            barcodeListener: ((HoLeeSheetDialog, String) -> Unit)? = null,

            @LayoutRes customLayout: Int? = null,
            viewStubCallBack: ((ViewStub, HoLeeSheetDialog) -> Unit)? = null

        ) =
            HoLeeSheetDialog().apply {
                this.onPositiveClickButton = onPositiveClickButton
                this.onNegativeClickButton = onNegativeClickButton
                this.inputListener = inputListener
                this.onCancelListener = onCancelListener
                this.onPickerNumberListener = onPickerNumberListener
                this.dropdownItems = dropdownItems
                this.onSelectorListener = onSelectorListener
                this.viewStubCallBack = viewStubCallBack
                this.barcodeListener = barcodeListener
                arguments = Bundle().apply {
                    icon?.let { putInt(ARG_ICON, it) }
                    putString(ARG_TITLE, title)
                    putCharSequence(ARG_MESSAGE, message)
                    putBoolean(ARG_CANCEL_ON_TOUCH_OUTSIDE, cancelOnTouchOutSide)
                    putBoolean(ARG_CANCELABLE, cancelable)
                    lottieFile?.let { putInt(ARG_LOTTIE_FILE, it) }
                    putBoolean(ARG_LOTTIE_LOOP, lottieLoop)
                    theme?.let { putInt(ARG_THEME, it) }
                    putString(ARG_POSITIVE_TEXT_BUTTON, positiveTextButton)
                    putString(ARG_NEGATIVE_TEXT_BUTTON, negativeTextButton)
                    putBoolean(ARG_CONTROL_DISMISS, controlDismiss)
                    inputType?.let { putInt(ARG_INPUT_TYPE, it) }
                    putString(ARG_INPUT_HINT, textHint)
                    putBoolean(ARG_END_ICON_CLEAR_TEXT, endIconClearText)
                    imeOptions?.let { putInt(ARG_IME_OPTIONS, it) }
                    imagenFile?.let { putInt(ARG_IMAGEN_FILE, it) }
                    typeLayout?.let { putSerializable(ARG_TYPE_LAYOUT, it) }
                    customLayout?.let { putInt(ARG_CUSTOM_LAYOUT, it) }
                    textInput?.let { putString(ARG_TEXTO_INPUT, it) }
                    errorMessage?.let { putString(ARG_ERROR_MESSAGE, it) }
                    bitmap?.let { putParcelable(ARG_BITMAP, it) }
                    initialValuePicker?.let { putInt(ARG_INITIAL_VALUE_PICKER, it) }
                    maxValue?.let { putInt(ARG_MAX_VALUE, it) }
                    minValue?.let { putInt(ARG_MIN_VALUE, it) }
                }
            }

        fun newInstanceBuilder(builder: Builder) =
            when (builder) {
                is Builder.Info -> {
                    newInstance(
                        icon = builder.icon,
                        title = builder.title?:"No has puesto titulo",
                        message = builder.message,
                        cancelOnTouchOutSide = builder.cancelOnTouchOutSide,
                        cancelable = builder.cancelable,
                        onCancelListener = builder.onCancelListener,
                        theme = builder.theme,
                        typeLayout = TypeLayout.INFO,

                    )
                }
                is Builder.LottieOrImage -> {
                    newInstance(
                        icon = builder.icon,
                        title = builder.title?:"No has puesto titulo",
                        message = builder.message,
                        lottieFile = builder.lottieFile,
                        lottieLoop = builder.lottieLoop,
                        cancelOnTouchOutSide = builder.cancelOnTouchOutSide,
                        cancelable = builder.cancelable,
                        positiveTextButton = builder.positiveTextButton,
                        onPositiveClickButton = builder.positiveListener,
                        theme = builder.theme,
                        imagenFile = builder.imageFile,
                        bitmap = builder.bitmap,
                        onCancelListener = builder.onCancelListener,
                        typeLayout = if (builder.lottieFile != null) TypeLayout.LOTTIE else TypeLayout.IMAGE
                    )
                }
                is Builder.Action -> {
                    newInstance(
                        icon = builder.icon,
                        title = builder.title?:"No has puesto titulo",
                        message = builder.message,
                        positiveTextButton = builder.positiveTextButton,
                        cancelOnTouchOutSide = builder.cancelOnTouchOutSide,
                        cancelable = builder.cancelable,
                        theme = builder.theme,
                        controlDismiss = builder.controlDismiss,
                        onPositiveClickButton = builder.positiveListener,
                        onNegativeClickButton = builder.negativeListener,
                        negativeTextButton = builder.negativeTextButton,
                        onCancelListener = builder.onCancelListener,
                        typeLayout = TypeLayout.ACTION,

                    )
                }
                is Builder.Input -> {
                    newInstance(
                        icon = builder.icon,
                        title = builder.title ?: "No has puesto titulo",
                        message = builder.message,
                        positiveTextButton = builder.positiveTextButton,
                        negativeTextButton = builder.negativeTextButton,
                        cancelOnTouchOutSide = builder.cancelOnTouchOutSide,
                        cancelable = builder.cancelable,
                        theme = builder.theme,
                        controlDismiss = builder.controlDismiss,
                        inputListener = builder.inputListener,
                        inputType = builder.inputType,
                        textHint = builder.textHint,
                        endIconClearText = builder.endIconClearText,
                        imeOptions = builder.imeOptions,
                        onCancelListener = builder.onCancelListener,
                        onNegativeClickButton = builder.negativeListener,
                        typeLayout = TypeLayout.INPUT,
                        textInput = builder.texto,
                        errorMessage = builder.errorMessage
                    )
                }
                is Builder.Selector -> {
                    newInstance(
                        icon = builder.icon,
                        title = builder.title ?: "No has puesto titulo",
                        message = builder.message,
                        positiveTextButton = builder.positiveTextButton,
                        negativeTextButton = builder.negativeTextButton,
                        cancelOnTouchOutSide = builder.cancelOnTouchOutSide,
                        cancelable = builder.cancelable,
                        theme = builder.theme,
                        controlDismiss = builder.controlDismiss,
                        textHint = builder.textHint,
                        onCancelListener = builder.onCancelListener,
                        onNegativeClickButton = builder.negativeListener,
                        typeLayout = TypeLayout.SELECTOR,
                        dropdownItems = builder.dropdownItems,
                        onSelectorListener = builder.dropdownListener
                    )
                }
                is Builder.PickerNumber -> {
                    newInstance(
                        icon = builder.icon,
                        title = builder.title ?: "No has puesto titulo",
                        message = builder.message,
                        positiveTextButton = builder.positiveTextButton,
                        cancelOnTouchOutSide = builder.cancelOnTouchOutSide,
                        negativeTextButton = builder.negativeTextButton,
                        onNegativeClickButton = builder.negativeListener,
                        cancelable = builder.cancelable,
                        theme = builder.theme,
                        controlDismiss = builder.controlDismiss,
                        onPickerNumberListener = builder.onPickerNumberListener,
                        onCancelListener = builder.onCancelListener,
                        initialValuePicker = builder.initialValue,
                        typeLayout = TypeLayout.PICKER_NUMBER,
                        maxValue = builder.maxValue,
                        minValue = builder.minValue,
                    )
                }
                is Builder.Custom -> {
                    newInstance(
                        icon = builder.icon,
                        title = builder.title ?: "No has puesto titulo",
                        message = builder.message,
                        positiveTextButton = builder.positiveTextButton,
                        cancelOnTouchOutSide = builder.cancelOnTouchOutSide,
                        negativeTextButton = builder.negativeTextButton,
                        onNegativeClickButton = builder.negativeListener,
                        cancelable = builder.cancelable,
                        theme = builder.theme,
                        controlDismiss = builder.controlDismiss,
                        onCancelListener = builder.onCancelListener,
                        onPositiveClickButton = builder.positiveListener,
                        typeLayout = TypeLayout.CUSTOM,
                        customLayout = builder.customLayout,
                        viewStubCallBack = builder.viewStubCallBack

                    )
                }
                is Builder.Barcode->{
                    newInstance(
                        icon = builder.icon,
                        title = builder.title ?: "No has puesto titulo",
                        message = builder.message,
                        positiveTextButton = builder.positiveTextButton,
                        cancelOnTouchOutSide = builder.cancelOnTouchOutSide,
                        cancelable = builder.cancelable,
                        theme = builder.theme,
                        controlDismiss = builder.controlDismiss,
                        onCancelListener = builder.onCancelListener,
                        typeLayout = TypeLayout.BARCODE,
                        barcodeListener = builder.onBarcodeListener,
                        inputType = builder.inputType,
                        textHint = builder.textHint,
                        endIconClearText = builder.endIconClearText,
                        imeOptions = builder.imeOptions,
                        errorMessage = builder.errorMessage,
                        textInput = builder.texto,

                    )
                }
            }
    }

    override fun getTheme(): Int = customTheme.takeIf { it != 0 } ?: R.style.HoLeeSheetBaseTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            icon = it.getInt(ARG_ICON)
            title = it.getString(ARG_TITLE)
            message = it.getCharSequence(ARG_MESSAGE)
            cancelOnTouchOutSide = it.getBoolean(ARG_CANCEL_ON_TOUCH_OUTSIDE)
            cancel = it.getBoolean(ARG_CANCELABLE)
            controlDismiss = it.getBoolean(ARG_CONTROL_DISMISS)
            lottieFile = it.getInt(ARG_LOTTIE_FILE)
            lottieLoop = it.getBoolean(ARG_LOTTIE_LOOP)
            positiveTextButton = it.getString(ARG_POSITIVE_TEXT_BUTTON)
            negativeTextButton = it.getString(ARG_NEGATIVE_TEXT_BUTTON)
            customTheme = it.getInt(ARG_THEME, R.style.HoLeeSheetBaseTheme)
            inputType = it.getInt(ARG_INPUT_TYPE)
            textHint = it.getString(ARG_INPUT_HINT)
            endIconClearText = it.getBoolean(ARG_END_ICON_CLEAR_TEXT)
            imeOptions = it.getInt(ARG_IME_OPTIONS)
            imagenFile = it.getInt(ARG_IMAGEN_FILE)
            typeLayout =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    it.getSerializable(ARG_TYPE_LAYOUT, TypeLayout::class.java)
                else   it.getSerializable(ARG_TYPE_LAYOUT) as TypeLayout
            customLayout = it.getInt(ARG_CUSTOM_LAYOUT)
            textoInput = it.getString(ARG_TEXTO_INPUT)
            errorMessage = it.getString(ARG_ERROR_MESSAGE)
            bitmap =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    it.getParcelable(ARG_BITMAP, Bitmap::class.java)
                else it.getParcelable(ARG_BITMAP)
            initialValuePicker = it.getInt(ARG_INITIAL_VALUE_PICKER)
            minValue = it.getInt(ARG_MIN_VALUE)
            maxValue = it.getInt(ARG_MAX_VALUE)
        }
    }
    override fun onCancel(dialog: DialogInterface) {
        onCancelListener?.invoke()
        barcodeView?.pause()
        super.onCancel(dialog)
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HoLeeSheetLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val rootView = View.inflate(context, R.layout.ho_lee_sheet_layout, null)
            val dialog = BottomSheetDialog(requireContext(), theme).also { dx ->
                isCancelable = cancel
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
                                        this@HoLeeSheetDialog.dismiss()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCommonViews()
        setupButtons()
        initUiState()
        when (typeLayout) {
            TypeLayout.LOTTIE -> setupViewLottie()
            TypeLayout.IMAGE -> setupViewImage()
            TypeLayout.INPUT -> setupViewInput()
            TypeLayout.PICKER_NUMBER -> setupViewPickerNumber()
            TypeLayout.SELECTOR -> setupViewSelector()
            TypeLayout.CUSTOM -> setupViewCustom()
            TypeLayout.BARCODE -> setupViewBarcode()
            else -> {}
        }
    }

    private fun initUiState() =
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.flowWithLifecycle(
                lifecycle = viewLifecycleOwner.lifecycle,
                minActiveState = Lifecycle.State.STARTED,
            ).collectLatest { state ->
                when (state) {
                    is UiState.Initial -> {
                        binding.btnCancelarHoLeeSheet.enabled()
                        binding.dxProgressBar.hide()
                    }
                    is UiState.Reset -> {
                        binding.btnAceptarHoLeeSheet.enabled()
                        binding.btnCancelarHoLeeSheet.enabled()
//                        inputLayout?.error = null
                        binding.dxProgressBar.hide()
                    }
                    is UiState.Loading -> {
                        binding.btnAceptarHoLeeSheet.disabled()
                        binding.btnCancelarHoLeeSheet.disabled()
                        binding.dxProgressBar.visible()
                    }
                    is UiState.OnScannerClickListener -> {
                        viewModel.newUiState(UiState.Loading)
                        barcodeListener?.invoke(this@HoLeeSheetDialog, viewModel.textoInput.value.orEmpty())
                        if (!controlDismiss) viewModel.newUiState(UiState.Hide)
                    }
                    is UiState.OnClickPositiveButton -> {
                        viewModel.newUiState(UiState.Loading)
                        onPositiveClickButton?.invoke(this@HoLeeSheetDialog)
                        if (!controlDismiss) viewModel.newUiState(UiState.Hide)
                    }
                    is UiState.OnInputClickListener -> {
                        viewModel.newUiState(UiState.Loading)
                        inputListener?.invoke(this@HoLeeSheetDialog, viewModel.textoInput.value.orEmpty())
                        if (!controlDismiss) viewModel.newUiState(UiState.Hide)
                    }
                    is UiState.OnClickNegativeButton -> {
                        viewModel.newUiState(UiState.Loading)
                        onNegativeClickButton?.invoke(this@HoLeeSheetDialog)
                        if (!controlDismiss) viewModel.newUiState(UiState.Hide)
                    }
                    is UiState.OnNumPickerClickListener -> {
                        viewModel.newUiState(UiState.Loading)
                        onPickerNumberListener?.invoke(this@HoLeeSheetDialog, viewModel.numPicker.value ?: 0)
                        if (!controlDismiss) viewModel.newUiState(UiState.Hide)
                    }
                    is UiState.OnDropDownClickListener -> {
                        viewModel.newUiState(UiState.Loading)
                        onSelectorListener?.invoke(this@HoLeeSheetDialog, viewModel.dropSelecction.value)
                        if (!controlDismiss) viewModel.newUiState(UiState.Hide)
                    }
                    is UiState.Hide -> {
                        barcodeView?.pause()
                        this@HoLeeSheetDialog.dismiss()
                    }
                }
            }
        }

    /*
     * METODOS PUBLICOS
     */
    fun show(fragmentManager: FragmentManager) = show(fragmentManager, TAG)
    fun dissmis() = viewModel.newUiState(UiState.Hide)
    fun resetUiState() = viewModel.newUiState(UiState.Reset)
    fun setInputError(msg:String) = viewModel.setTextoErrorInput(msg)

    private fun ocultarLector() {
        barcodeContainer?.goneAlpha()
        inputLayout?.visibleAlpha()
        binding.btnCancelarHoLeeSheet.text = "Escanear"
        barcodeView?.pause()
    }

    private fun mostrarLector() {
        binding.btnCancelarHoLeeSheet.text = "Cerrar"
        inputLayout?.goneAlpha()
        barcodeContainer?.visibleAlpha()
        IntentIntegrator.forSupportFragment(this@HoLeeSheetDialog)
            .also {
                it.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                it.setCameraId(0)
                it.setBeepEnabled(true)
                it.setOrientationLocked(true)
                it.setBarcodeImageEnabled(true)
            }

        barcodeView?.resume()
        leerCodigo()
    }

    private fun leerCodigo(){
        barcodeView?.decodeContinuous { result ->
            barcodeView?.pause()
            viewModel.setValorScanner(result.text)
            Log.w("BARCODE", "leerCodigo: ${result.text}")
            ocultarLector()
        }
    }
    private fun setupViewBarcode() = with(binding) {
        btnAceptarHoLeeSheet.isEnabled = false
        viewStub.layoutResource = R.layout.barcode_layout
        var txtInput: TextInputEditText? = null
        viewStub.inflate().also { vs ->

            barcodeView = vs.findViewById<BarcodeView>(R.id.barcode_view)
            barcodeContainer = vs.findViewById<MaterialCardView>(R.id.barcode_container)

            inputLayout = vs.findViewById<TextInputLayout>(R.id.input_layout_dx)
                .apply {
                    this.hint = textHint.orEmpty()
                    this.endIconMode = if (endIconClearText) TextInputLayout.END_ICON_CLEAR_TEXT else TextInputLayout.END_ICON_NONE
                }
            txtInput = vs.findViewById<TextInputEditText>(R.id.txt_input_dx)
                .apply {
                    this@HoLeeSheetDialog.inputType?.let { this.inputType = it }
                    this@HoLeeSheetDialog.imeOptions?.let { this.imeOptions = it }
                    this.setText(textoInput.orEmpty())
                    this.doOnTextChanged { text, _, _, _ ->
                        viewModel.setTextoInput(text.toString())
                        errorMessage?.let { msg->
                            inputLayout?.error = if (text.isNullOrBlank()) msg else null
                        }
                        btnAceptarHoLeeSheet.isEnabled = text?.toString()?.isNotEmpty() ?: false
                    }
                }
        }
        viewModel.textoErrorInput.observe(viewLifecycleOwner) { error ->
            inputLayout?.error = error
        }
        viewModel.valorScanner.observe(viewLifecycleOwner) { value ->
            value?.let { txtInput?.setText(it) }
        }
        btnCancelarHoLeeSheet.apply {
            text = "Escanear"
            icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_barcode, null)
            iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            setIconTintResource(R.color.btn_text_sheet_selector)
            setOnClickListener {
                if (barcodeContainer?.isVisible == true) ocultarLector()
                else mostrarLector()
            }
        }
        mostrarLector()
    }

    private fun setupViewCustom() = binding.apply {
        customLayout.takeIf { it != 0 }?.let {
            viewStub.layoutResource = it
        }
        viewStubCallBack?.invoke(viewStub, this@HoLeeSheetDialog)
    }

    private fun setupCommonViews() = binding.apply {
        icon.takeIf { it != 0 }?.let { imgIconHoLeeSheet.setImageResource(it) }
        tvTituloHoLeeSheet.text = title
        txtMessageHoLeeSheet.apply {
            show(message != null)
            message?.let { text = it }
        }
    }
    private fun setupButtons() = binding.apply {
        containerButtonsHoLeeSheet.show(
            onPositiveClickButton != null || onNegativeClickButton != null || inputListener != null ||
                    onPickerNumberListener != null || onSelectorListener != null || barcodeListener != null)
        btnAceptarHoLeeSheet.apply {
            show(onPositiveClickButton != null || inputListener != null || onPickerNumberListener != null || onSelectorListener != null || barcodeListener != null)
            text = positiveTextButton.orEmpty()
            setOnClickListener {
                when {
                    inputListener != null -> viewModel.newUiState(UiState.OnInputClickListener)
                    onPickerNumberListener != null ->viewModel.newUiState(UiState.OnNumPickerClickListener)
                    onSelectorListener != null -> viewModel.newUiState(UiState.OnDropDownClickListener)
                    barcodeListener != null -> viewModel.newUiState(UiState.OnScannerClickListener)
                    else -> viewModel.newUiState(UiState.OnClickPositiveButton)
                }
            }
        }

        btnCancelarHoLeeSheet.apply {
            show(onNegativeClickButton != null || barcodeListener != null)
            text = negativeTextButton.orEmpty()
            setOnClickListener {
                viewModel.newUiState(UiState.OnClickNegativeButton)
            }
        }

    }
    private fun setupViewPickerNumber() = binding.apply {
        viewStub.layoutResource = R.layout.picker_number_layout
        viewModel.setNumPicker(initialValuePicker?:0)
        viewStub.inflate().also {
            it.findViewById<ImageView>(R.id.picker_minus).apply {
                setOnClickListener {
                    minValue?.let { minimo ->
                        if (viewModel.numPicker.value!! > minimo)
                            viewModel.minusNumPicker()
                        else Toast.makeText(requireContext(), "No puedes seleccionar menos de $minimo", Toast.LENGTH_SHORT).show()
                    } ?: kotlin.run { viewModel.minusNumPicker() }
                }
            }
            it.findViewById<ImageView>(R.id.picker_mas).apply {
                setOnClickListener {
                    maxValue?.let { maximo ->
                        if (viewModel.numPicker.value!! < maximo)
                            viewModel.plusNumPicker()
                        else Toast.makeText(requireContext(), "No puedes seleccionar más de $maximo", Toast.LENGTH_SHORT).show()
                    }?: kotlin.run { viewModel.plusNumPicker() }
                }
            }
            it.findViewById<TextView>(R.id.tv_number_picker).apply {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.numPicker.observe(viewLifecycleOwner) {txt ->
                        text = txt.toString()
                    }
                }
            }
        }
    }
    private fun setupViewImage() = binding.apply {
        viewStub.layoutResource = R.layout.image_layout
        viewStub.inflate().also { vs ->
            vs.findViewById<ImageView>(R.id.img_icon_ho_lee_sheet).apply {
                imagenFile.takeIf { it != 0 }?.let { img ->
                    setImageResource(img)
                }
                bitmap?.let { bmp -> setImageBitmap(bmp) }
            }
        }


    }
    private fun setupViewLottie() = binding.apply {
        lottieFile.takeIf { it != 0 }?.let { lottie ->
            viewStub.layoutResource = R.layout.lottie_layout
            viewStub.inflate().also {
                it.findViewById<LottieAnimationView>(R.id.lottie_ho_lee_sheet)
                    .apply{
                        this.setAnimation(lottie)
                        this.repeatCount = if (lottieLoop) LottieDrawable.INFINITE else 0
                        postDelayed({ this.playAnimation() }, 600)
                    }

            }
        }
    }
    private fun setupViewInput() = binding.apply {
        btnAceptarHoLeeSheet.isEnabled = false
        viewStub.layoutResource = R.layout.input_layout
        viewStub.inflate().also { vs ->
            inputLayout = vs.findViewById<TextInputLayout>(R.id.input_layout_ho_lee_sheet)
                .apply {
                    this.hint = textHint.orEmpty()
                    this.endIconMode = if (endIconClearText) TextInputLayout.END_ICON_CLEAR_TEXT else TextInputLayout.END_ICON_NONE

                }
            vs.findViewById<TextInputEditText>(R.id.txt_input_ho_lee_sheet)
                .apply {
                    this@HoLeeSheetDialog.inputType?.let { this.inputType = it }
                    this@HoLeeSheetDialog.imeOptions?.let { this.imeOptions = it }
                    this.setText(textoInput.orEmpty())
                    this.doOnTextChanged { text, _, _, _ ->
                        viewModel.setTextoInput(text.toString())
                        errorMessage?.let { msg->
                            inputLayout?.error = if (text.isNullOrEmpty()) msg else null
                        }
                        btnAceptarHoLeeSheet.isEnabled = text?.toString()?.isNotEmpty() ?: false
                    }
                }
        }
        viewModel.textoErrorInput.observe(viewLifecycleOwner) { error ->
            inputLayout?.error = error
        }

    }

    private fun setupViewSelector() = binding.apply {
        viewStub.layoutResource = R.layout.dropdown_layout
        viewStub.inflate().also { vs ->
            vs.findViewById<TextInputLayout>(R.id.dropdown_layout_ho_lee_sheet)
                .apply {this.hint = textHint.orEmpty() }

            vs.findViewById<AutoCompleteTextView>(R.id.dropdown_ho_lee_shee)
                .apply {
                    setAdapter(ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, dropdownItems?.toMutableList().orEmpty()))
                    setOnClickListener { showDropDown() }
                    setOnItemClickListener { parent, view, position, id ->
                        viewModel.setDropSelecction(position)
                    }
                }
        }
        viewModel.dropSelecction.observe(viewLifecycleOwner) {
            btnAceptarHoLeeSheet.isEnabled = it != null
        }
    }




    sealed class Builder {

        internal var icon: Int? = null
        internal var title: String? = null
        internal var message: CharSequence? = null
        internal var cancelOnTouchOutSide: Boolean = true
        internal var cancelable: Boolean = true
        internal var controlDismiss: Boolean = false
        internal var theme: Int? = null

        internal var positiveTextButton: String? = null
        internal var positiveListener: ((HoLeeSheetDialog) -> Unit)? = null

        internal var negativeTextButton: String? = null
        internal var negativeListener: ((HoLeeSheetDialog) -> Unit)? = null

        internal var onCancelListener: (() -> Unit)? = null

        private fun build() = newInstanceBuilder(this)

        fun buildAndShow(fragmentManager: FragmentManager) =
            fragmentManager.findFragmentByTag(TAG) ?: run { build().show(fragmentManager) }

        @Override protected abstract fun setIcon(@DrawableRes icon: Int): Builder
        @Override protected abstract fun setTitle(title: String): Builder
        @Override protected abstract fun setMessage(message: CharSequence): Builder
        @Override protected abstract fun setCancelOnTouchOutSide(cancelOnTouchOutSide: Boolean): Builder
        @Override protected abstract fun setCancelable(cancelable: Boolean): Builder
        @Override protected abstract fun setTheme(@StyleRes theme: Int): Builder
        @Override protected abstract fun setControlDismiss(controlDismiss: Boolean) : Builder
        @Override protected abstract fun setOnCancelListener(onCancelListener: (() -> Unit)?): Builder


        class Info : Builder() {
            public override fun setIcon(@DrawableRes icon: Int) = apply { this.icon = icon }
            public override fun setTitle(title: String) = apply { this.title = title }
            public override fun setMessage(message: CharSequence) = apply { this.message = message }
            public override fun setCancelOnTouchOutSide(cancelOnTouchOutSide: Boolean) = apply { this.cancelOnTouchOutSide = cancelOnTouchOutSide }
            public override fun setCancelable(cancelable: Boolean) = apply { this.cancelable = cancelable }
            public override fun setTheme(@StyleRes theme: Int) = apply { this.theme = theme }
            public override fun setControlDismiss(controlDismiss: Boolean) = apply { this.controlDismiss = controlDismiss }
            public override fun setOnCancelListener(onCancelListener: (() -> Unit)?) = apply { this.onCancelListener = onCancelListener }

        }
        class LottieOrImage : Builder() {

            internal var lottieFile: Int? = null
            internal var lottieLoop: Boolean = true
            internal var imageFile: Int? = null
            internal var bitmap: Bitmap? = null

            public override fun setIcon(@DrawableRes icon: Int) = apply { this.icon = icon }
            public override fun setTitle(title: String) = apply { this.title = title }
            public override fun setMessage(message: CharSequence) = apply { this.message = message }
            public override fun setCancelOnTouchOutSide(cancelOnTouchOutSide: Boolean) = apply { this.cancelOnTouchOutSide = cancelOnTouchOutSide }
            public override fun setCancelable(cancelable: Boolean) = apply { this.cancelable = cancelable }
            public override fun setControlDismiss(controlDismiss: Boolean) = apply { this.controlDismiss = controlDismiss }

            public override fun setTheme(@StyleRes theme: Int) = apply { this.theme = theme }
            fun setLottie(@RawRes lottieRaw: Int) = apply { this.lottieFile = lottieRaw }
            fun setLottieLoop(lottieLoop: Boolean) = apply { this.lottieLoop = lottieLoop }
            fun setImageResource(@DrawableRes image: Int) = apply { this.imageFile = image }

            fun setImageBitmap(bitmap: Bitmap) = apply { this.bitmap = bitmap }

            public override fun setOnCancelListener(onCancelListener: (() -> Unit)?) = apply { this.onCancelListener = onCancelListener }

            fun setPositiveButton(textButton: String, onPositiveClickListener: ((HoLeeSheetDialog) -> Unit)?) = apply {
                this.positiveTextButton = textButton
                this.positiveListener = onPositiveClickListener
            }
            fun setNegativeButton(textButton: String, onNegativeClickListener: ((HoLeeSheetDialog) -> Unit)?) = apply {
                this.negativeTextButton = textButton
                this.negativeListener = onNegativeClickListener
            }


        }
        class Action : Builder() {

            public override fun setIcon(@DrawableRes icon: Int) = apply { this.icon = icon }
            public override fun setTitle(title: String) = apply { this.title = title }
            public override fun setMessage(message: CharSequence) = apply { this.message = message }
            public override fun setCancelOnTouchOutSide(cancelOnTouchOutSide: Boolean) = apply { this.cancelOnTouchOutSide = cancelOnTouchOutSide }
            public override fun setCancelable(cancelable: Boolean) = apply { this.cancelable = cancelable }
            public override fun setControlDismiss(controlDismiss: Boolean) = apply { this.controlDismiss = controlDismiss }
            public override fun setTheme(@StyleRes theme: Int) = apply { this.theme = theme }

            public override fun setOnCancelListener(onCancelListener: (() -> Unit)?) = apply { this.onCancelListener = onCancelListener }

            fun setPositiveButton(textButton: String, onPositiveClickListener: ((HoLeeSheetDialog) -> Unit)?) = apply {
                this.positiveTextButton = textButton
                this.positiveListener = onPositiveClickListener
            }
            fun setNegativeButton(textButton: String, onNegativeClickListener: ((HoLeeSheetDialog) -> Unit)?) = apply {
                this.negativeTextButton = textButton
                this.negativeListener = onNegativeClickListener
            }

        }
        class Input : Builder() {

            internal var inputType : Int? = null
            internal var textHint : String? = null
            internal var texto: String? = null
            internal var endIconClearText : Boolean = true
            internal var imeOptions : Int? = null
            internal var inputListener: ((HoLeeSheetDialog, String) -> Unit)? = null
            internal var errorMessage : String? = null

            public override fun setIcon(@DrawableRes icon: Int) = apply { this.icon = icon }
            public override fun setTitle(title: String) = apply { this.title = title }
            public override fun setMessage(message: CharSequence) = apply { this.message = message }
            public override fun setCancelOnTouchOutSide(cancelOnTouchOutSide: Boolean) = apply { this.cancelOnTouchOutSide = cancelOnTouchOutSide }
            public override fun setCancelable(cancelable: Boolean) = apply { this.cancelable = cancelable }
            public override fun setTheme(@StyleRes theme: Int) = apply { this.theme = theme }
            public override fun setControlDismiss(controlDismiss: Boolean) = apply { this.controlDismiss = controlDismiss }
            public override fun setOnCancelListener(onCancelListener: (() -> Unit)?) = apply { this.onCancelListener = onCancelListener }

            fun setInputListener(textButton: String, onInputListener: ((HoLeeSheetDialog, String) -> Unit)?) = apply {
                this.positiveTextButton = textButton
                this.inputListener = onInputListener
            }

            fun setNegativeButton(textButton: String, onNegativeClickListener: ((HoLeeSheetDialog) -> Unit)?) = apply {
                this.negativeTextButton = textButton
                this.negativeListener = onNegativeClickListener
            }


            fun setText(texto: String) = apply { this.texto = texto }
            fun setInputType (inputType: Int) = apply { this.inputType = inputType }
            fun setTextHint (texto : String) = apply { this.textHint = texto }
            fun setEndIconClearText (value: Boolean) = apply { this.endIconClearText = value }
            fun setImeOptions (editorInfo: Int) = apply { this.imeOptions = editorInfo }
            fun setErrorMessage (message: String) = apply { this.errorMessage = message }

        }
        class PickerNumber: Builder() {

            internal var onPickerNumberListener: ((HoLeeSheetDialog, Int) -> Unit)? = null
            internal var initialValue : Int? = null
            internal var minValue: Int? = null
            internal var maxValue: Int? = null

            public override fun setIcon(@DrawableRes icon: Int) = apply { this.icon = icon }
            public override fun setTitle(title: String) = apply { this.title = title }
            public override fun setMessage(message: CharSequence) = apply { this.message = message }
            public override fun setCancelOnTouchOutSide(cancelOnTouchOutSide: Boolean) = apply { this.cancelOnTouchOutSide = cancelOnTouchOutSide }
            public override fun setCancelable(cancelable: Boolean) = apply { this.cancelable = cancelable }
            public override fun setTheme(@StyleRes theme: Int) = apply { this.theme = theme }
            public override fun setControlDismiss(controlDismiss: Boolean) = apply { this.controlDismiss = controlDismiss }
            public override fun setOnCancelListener(onCancelListener: (() -> Unit)?) = apply { this.onCancelListener = onCancelListener }

            fun setInitialValue (value: Int) = apply { this.initialValue = value }
            fun setMinValue (value: Int) = apply { this.minValue = value }
            fun setMaxalue (value: Int) = apply { this.maxValue = value }

            fun setOnPickerNumberListener(textButton: String, onPickerNumberListener: ((HoLeeSheetDialog, Int) -> Unit)?) = apply {
                this.positiveTextButton = textButton
                this.onPickerNumberListener = onPickerNumberListener
            }

            fun setNegativeButton(textButton: String, onNegativeClickListener: ((HoLeeSheetDialog) -> Unit)?) = apply {
                this.negativeTextButton = textButton
                this.negativeListener = onNegativeClickListener
            }


        }
        class Selector: Builder() {

            internal var dropdownItems: List<String>? = null
            internal var textHint: String? = null
            internal var dropdownListener : ((HoLeeSheetDialog, Int?) -> Unit)? = null
            public override fun setIcon(@DrawableRes icon: Int) = apply { this.icon = icon }
            public override fun setTitle(title: String) = apply { this.title = title }
            public override fun setMessage(message: CharSequence) = apply { this.message = message }
            public override fun setCancelOnTouchOutSide(cancelOnTouchOutSide: Boolean) = apply { this.cancelOnTouchOutSide = cancelOnTouchOutSide }
            public override fun setCancelable(cancelable: Boolean) = apply { this.cancelable = cancelable }
            public override fun setTheme(@StyleRes theme: Int) = apply { this.theme = theme }
            public override fun setControlDismiss(controlDismiss: Boolean) = apply { this.controlDismiss = controlDismiss }
            public override fun setOnCancelListener(onCancelListener: (() -> Unit)?) = apply { this.onCancelListener = onCancelListener }

            fun setHint(textHint: String) = apply { this.textHint = textHint }
            fun setNegativeButton(textButton: String, onNegativeClickListener: ((HoLeeSheetDialog) -> Unit)?) = apply {
                this.negativeTextButton = textButton
                this.negativeListener = onNegativeClickListener
            }

            fun setDropdownListener(textButton: String, onDropdownListener: ((HoLeeSheetDialog, Int?) -> Unit)?) = apply {
                this.positiveTextButton = textButton
                this.dropdownListener = onDropdownListener
            }
            fun setDropdownItems (items: List<String>) = apply { this.dropdownItems = items }
        }
        class Custom : Builder() {

            internal var customLayout: Int? = null
            internal var viewStubCallBack :( (ViewStub, HoLeeSheetDialog) -> Unit)? = null
            public override fun setIcon(@DrawableRes icon: Int) = apply { this.icon = icon }
            public override fun setTitle(title: String) = apply { this.title = title }
            public override fun setMessage(message: CharSequence) = apply { this.message = message }
            public override fun setCancelOnTouchOutSide(cancelOnTouchOutSide: Boolean) = apply { this.cancelOnTouchOutSide = cancelOnTouchOutSide }
            public override fun setCancelable(cancelable: Boolean) = apply { this.cancelable = cancelable }
            public override fun setTheme(@StyleRes theme: Int) = apply { this.theme = theme }
            public override fun setControlDismiss(controlDismiss: Boolean) = apply { this.controlDismiss = controlDismiss }
            public override fun setOnCancelListener(onCancelListener: (() -> Unit)?) = apply { this.onCancelListener = onCancelListener }

            fun setPositiveButton(textButton: String, onPositiveClickListener: ((HoLeeSheetDialog) -> Unit)?) = apply {
                this.positiveTextButton = textButton
                this.positiveListener = onPositiveClickListener
            }
            fun setNegativeButton(textButton: String, onNegativeClickListener: ((HoLeeSheetDialog) -> Unit)?) = apply {
                this.negativeTextButton = textButton
                this.negativeListener = onNegativeClickListener
            }

            fun setCustomLayout(@LayoutRes customLayout: Int, viewStubCallBack: (ViewStub, dx: HoLeeSheetDialog) -> Unit ) = apply {
                this.customLayout = customLayout
                this.viewStubCallBack = viewStubCallBack
            }
        }
        class Barcode: Builder() {

            internal var onBarcodeListener: ((HoLeeSheetDialog, String) -> Unit)? = null
            internal var inputType : Int? = null
            internal var textHint : String? = null
            internal var texto: String? = null
            internal var endIconClearText : Boolean = true
            internal var imeOptions : Int? = null
            internal var errorMessage : String? = null
            public override fun setIcon(@DrawableRes icon: Int) = apply { this.icon = icon }
            public override fun setTitle(title: String) = apply { this.title = title }
            public override fun setMessage(message: CharSequence) = apply { this.message = message }
            public override fun setCancelOnTouchOutSide(cancelOnTouchOutSide: Boolean) = apply { this.cancelOnTouchOutSide = cancelOnTouchOutSide }
            public override fun setCancelable(cancelable: Boolean) = apply { this.cancelable = cancelable }
            public override fun setTheme(@StyleRes theme: Int) = apply { this.theme = theme }
            public override fun setControlDismiss(controlDismiss: Boolean) = apply { this.controlDismiss = controlDismiss }
            public override fun setOnCancelListener(onCancelListener: (() -> Unit)?) = apply { this.onCancelListener = onCancelListener }

            fun setPositiveButton(textButton: String, onBarcodeListener: ((HoLeeSheetDialog, String) -> Unit)?) = apply {
                this.positiveTextButton = textButton
                this.onBarcodeListener = onBarcodeListener
            }

            fun setText(texto: String) = apply { this.texto = texto }
            fun setInputType (inputType: Int) = apply { this.inputType = inputType }
            fun setTextHint (texto : String) = apply { this.textHint = texto }
            fun setEndIconClearText (value: Boolean) = apply { this.endIconClearText = value }
            fun setImeOptions (editorInfo: Int) = apply { this.imeOptions = editorInfo }

            fun setErrorMessage (message: String) = apply { this.errorMessage = message }

        }

    }


}


internal fun View.visible() { this.visibility = View.VISIBLE }
internal fun View.hide() { this.visibility = View.GONE }
internal fun View.show(show: Boolean) = if (show) visible() else hide()
internal fun View.goneAlpha() = this.animate().alpha(0f).setDuration(400).withEndAction { this.hide() }.start()
internal fun View.disabled() { this.isEnabled = false }
internal fun View.enabled() { this.isEnabled = true }
internal fun View.visibleAlpha() =
    this.apply {
        alpha = 0f
        visible()
        animate().alpha(1f).setDuration(400).start()
    }
