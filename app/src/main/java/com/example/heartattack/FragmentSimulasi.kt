package com.example.heartattack

import android.content.res.AssetManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import com.example.heartattack.R
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class FragmentSimulasi : Fragment() {

    private lateinit var interpreter: Interpreter
    private val mModelPath = "heartattack.tflite"

    private lateinit var resultText: TextView
    private lateinit var edtAge: EditText
    private lateinit var edtGender: EditText
    private lateinit var edtImpulse: EditText
    private lateinit var edtPressureHigh: EditText
    private lateinit var edtPressureLow: EditText
    private lateinit var edtGlucose: EditText
    private lateinit var edtKCM: EditText
    private lateinit var edtTroponin: EditText
    private lateinit var checkButton: Button

    private lateinit var clearAge: ImageView
    private lateinit var clearGender: ImageView
    private lateinit var clearImpulse: ImageView
    private lateinit var clearPressureHigh: ImageView
    private lateinit var clearPressureLow: ImageView
    private lateinit var clearGlucose: ImageView
    private lateinit var clearKCM: ImageView
    private lateinit var clearTroponin: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_simulasi, container, false)

        resultText = view.findViewById(R.id.txtResult)
        edtAge = view.findViewById(R.id.editAge)
        edtGender = view.findViewById(R.id.editGender)
        edtImpulse = view.findViewById(R.id.editImpulse)
        edtPressureHigh = view.findViewById(R.id.editPressureHigh)
        edtPressureLow = view.findViewById(R.id.editPressureLow)
        edtGlucose = view.findViewById(R.id.editGlucose)
        edtKCM = view.findViewById(R.id.editKCM)
        edtTroponin = view.findViewById(R.id.editTroponin)
        checkButton = view.findViewById(R.id.btnCheck)

        clearAge = view.findViewById(R.id.clearAge)
        clearGender = view.findViewById(R.id.clearGender)
        clearImpulse = view.findViewById(R.id.clearImpulse)
        clearPressureHigh = view.findViewById(R.id.clearPressureHigh)
        clearPressureLow = view.findViewById(R.id.clearPressureLow)
        clearGlucose = view.findViewById(R.id.clearGlucose)
        clearKCM = view.findViewById(R.id.clearKCM)
        clearTroponin = view.findViewById(R.id.clearTroponin)

        // Initialize TextWatchers
        initTextWatchers()

        // Set onClickListeners for clear icons
        clearAge.setOnClickListener {
            edtAge.text.clear()
            clearAge.visibility = View.INVISIBLE
        }

        clearGender.setOnClickListener {
            edtGender.text.clear()
            clearGender.visibility = View.INVISIBLE
        }

        clearImpulse.setOnClickListener {
            edtImpulse.text.clear()
            clearImpulse.visibility = View.INVISIBLE
        }

        clearPressureHigh.setOnClickListener {
            edtPressureHigh.text.clear()
            clearPressureHigh.visibility = View.INVISIBLE
        }

        clearPressureLow.setOnClickListener {
            edtPressureLow.text.clear()
            clearPressureLow.visibility = View.INVISIBLE
        }

        clearGlucose.setOnClickListener {
            edtGlucose.text.clear()
            clearGlucose.visibility = View.INVISIBLE
        }

        clearKCM.setOnClickListener {
            edtKCM.text.clear()
            clearKCM.visibility = View.INVISIBLE
        }

        clearTroponin.setOnClickListener {
            edtTroponin.text.clear()
            clearTroponin.visibility = View.INVISIBLE
        }

        checkButton.setOnClickListener {
            if (validateInputs()) {
                try {
                    val result = doInference(
                        edtAge.text.toString().toFloat(),
                        edtGender.text.toString().toFloat(),
                        edtImpulse.text.toString().toFloat(),
                        edtPressureHigh.text.toString().toFloat(),
                        edtPressureLow.text.toString().toFloat(),
                        edtGlucose.text.toString().toFloat(),
                        edtKCM.text.toString().toFloat(),
                        edtTroponin.text.toString().toFloat()
                    )
                    activity?.runOnUiThread {
                        resultText.text = if (result == 0) "No Heart Attack" else "Heart Attack"
                    }
                } catch (e: Exception) {
                    resultText.text = "Prediksi Gagal: ${e.message}"
                }
            } else {
                showAlertDialog()
            }
        }

        initInterpreter()
        return view
    }

    private fun initTextWatchers() {
        edtAge.addTextChangedListener(createTextWatcher(clearAge, edtAge))
        edtGender.addTextChangedListener(createTextWatcher(clearGender, edtGender))
        edtImpulse.addTextChangedListener(createTextWatcher(clearImpulse, edtImpulse))
        edtPressureHigh.addTextChangedListener(createTextWatcher(clearPressureHigh, edtPressureHigh))
        edtPressureLow.addTextChangedListener(createTextWatcher(clearPressureLow, edtPressureLow))
        edtGlucose.addTextChangedListener(createTextWatcher(clearGlucose, edtGlucose))
        edtKCM.addTextChangedListener(createTextWatcher(clearKCM, edtKCM))
        edtTroponin.addTextChangedListener(createTextWatcher(clearTroponin, edtTroponin))
    }

    private fun createTextWatcher(clearIcon: ImageView, editText: EditText): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearIcon.visibility = if (s.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun validateInputs(): Boolean {
        return edtAge.text.isNotEmpty() &&
                edtGender.text.isNotEmpty() &&
                edtImpulse.text.isNotEmpty() &&
                edtPressureHigh.text.isNotEmpty() &&
                edtPressureLow.text.isNotEmpty() &&
                edtGlucose.text.isNotEmpty() &&
                edtKCM.text.isNotEmpty() &&
                edtTroponin.text.isNotEmpty()
    }

    private fun showAlertDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("WARNING!!")
            .setMessage("Isi semua kolom input")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialog.show()
    }

    private fun initInterpreter() {
        val options = Interpreter.Options()
        options.setNumThreads(5)
        options.setUseNNAPI(true)
        interpreter = Interpreter(loadModelFile(requireContext().assets, mModelPath), options)
    }

    private fun doInference(
        age: Float, gender: Float, impulse: Float, pressureHigh: Float,
        pressureLow: Float, glucose: Float, kcm: Float, troponin: Float
    ): Int {
        val inputVal = floatArrayOf(
            age, gender, impulse, pressureHigh,
            pressureLow, glucose, kcm, troponin
        )
        val output = Array(1) { FloatArray(2) }
        interpreter.run(arrayOf(inputVal), output)
        Log.e("result", output[0].contentToString())
        return output[0].indexOfFirst { it == output[0].maxOrNull() }
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}
