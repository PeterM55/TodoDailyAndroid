package peter.mitchell.tododaily.ui.home

import android.os.Build
import android.text.InputType
import androidx.annotation.RequiresApi
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class SaveInformation {

    var dateFormat : SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
    enum class InformationFormat {
        checkBox, integer, onePointDecimal, twoPointDecimal, decimal, text
    }
    public val informationFormatStrings : Array<String> = arrayOf(
        "Check box",
        "Integer",
        "One point decimal",
        "Two point decimal",
        "Decimal",
        "Text",
    )

    var date : LocalDate = LocalDate.now()
    var length : Int = 0
    var names : ArrayList<String> = ArrayList(10)
    private var values : ArrayList<String> = ArrayList(10)
    var formats : ArrayList<InformationFormat> = ArrayList(10)
    private var timeRead : ArrayList<Long> = ArrayList(10)

    public fun addValue(name : String, infoFormat : InformationFormat) : Boolean {
        if (!verifyFormat("", infoFormat)) return false

        names.add(name)
        values.add("")
        formats.add(infoFormat)
        timeRead.add(0)
        length++
        return true
    }

    /** Check whether the string can fit the given format
     *
     * @param str the string to check
     * @param infoFormat the format to check if it is
     * @return whether it can fit that format
     */
    public fun verifyFormat(str : String, infoFormat : InformationFormat) : Boolean {

        if (str == "") return true

        if (infoFormat == InformationFormat.checkBox) {
            if (str != "1" && str != "0") {
                return false
            }
            return true
        } else if (infoFormat == InformationFormat.integer) {
            if (str.toIntOrNull() == null) {
                return false
            }
            return true
        } else if (
            infoFormat == InformationFormat.onePointDecimal ||
            infoFormat == InformationFormat.twoPointDecimal ||
            infoFormat == InformationFormat.decimal
        ) {
            if (str.toDoubleOrNull() == null) {
                return false
            }
            return true
        } else if (infoFormat == InformationFormat.text) {
            return true
        }
        return false
    }

    /** Get the value to display to the user for the index
     *
     * @param i the index to get
     * @return the string to display to the user
     */
    public fun getDisplayValue(i : Int) : String {
        if (values[i] == "" || !verifyFormat(values[i], formats[i])) {
            return "---"
        }

        if (
            formats[i] == InformationFormat.text ||
            formats[i] == InformationFormat.integer ||
            formats[i] == InformationFormat.decimal
        ) {
            return values[i]
        }

        if (formats[i] == InformationFormat.checkBox) {
            if (values[i] == "1") {
                return "✔"
            } else {
                return "X"
            }
        } else if (formats[i] == InformationFormat.onePointDecimal) {
            return String.format("%.1f", values[i].toDouble())
        } else if (formats[i] == InformationFormat.twoPointDecimal) {
            return String.format("%.2f", values[i].toDouble())
        }

        return values[i]
    }

    public fun setValue(i : Int, value : String) : Boolean {
        if (!verifyFormat(value, formats[i])) return false

        values[i] = value
        timeRead[i] = System.currentTimeMillis();

        return true
    }

    public fun toggleBox(i : Int) {
        if (values[i] == "1") {
            values[i] = "0"
            timeRead[i] = 0;
        } else {
            values[i] = "1"
            timeRead[i] = System.currentTimeMillis();
        }
    }

    public fun getInputType(i : Int): Int {
        if (i > formats.size)
            return InputType.TYPE_CLASS_TEXT;

        if (formats[i] == InformationFormat.checkBox) {
            return -1
        } else if (formats[i] == InformationFormat.integer) {
            return InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        } else if (formats[i] == InformationFormat.onePointDecimal) {
            return InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL
        } else if (formats[i] == InformationFormat.twoPointDecimal) {
            return InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL
        } else if (formats[i] == InformationFormat.decimal) {
            return InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL
        } else if (formats[i] == InformationFormat.text) {
            return InputType.TYPE_CLASS_TEXT
        }
        return InputType.TYPE_CLASS_TEXT
    }

    public fun informationFormatStringToEnum(str : String) : InformationFormat {
        var infoFormat : SaveInformation.InformationFormat = SaveInformation.InformationFormat.text

        if (str == informationFormatStrings[0]) {
            infoFormat = SaveInformation.InformationFormat.checkBox
        } else if (str == informationFormatStrings[1]) {
            infoFormat = SaveInformation.InformationFormat.integer
        } else if (str == informationFormatStrings[2]) {
            infoFormat = SaveInformation.InformationFormat.onePointDecimal
        } else if (str == informationFormatStrings[3]) {
            infoFormat = SaveInformation.InformationFormat.twoPointDecimal
        } else if (str == informationFormatStrings[4]) {
            infoFormat = SaveInformation.InformationFormat.decimal
        } else if (str == informationFormatStrings[5]) {
            infoFormat = SaveInformation.InformationFormat.text
        }

        return infoFormat
    }

    public fun informationFormatEnumToString(infoFormat : InformationFormat) : String {
        var str : String = informationFormatStrings[0]

        if (infoFormat == SaveInformation.InformationFormat.checkBox) {
            str = informationFormatStrings[0]
        } else if (infoFormat == SaveInformation.InformationFormat.integer) {
            str = informationFormatStrings[1]
        } else if (infoFormat == SaveInformation.InformationFormat.onePointDecimal) {
            str = informationFormatStrings[2]
        } else if (infoFormat == SaveInformation.InformationFormat.twoPointDecimal) {
            str = informationFormatStrings[3]
        } else if (infoFormat == SaveInformation.InformationFormat.decimal) {
            str = informationFormatStrings[4]
        } else if (infoFormat == SaveInformation.InformationFormat.text) {
            str = informationFormatStrings[5]
        }

        return str
    }

    public fun resetData() {
        date = LocalDate.now()
        length = 0
        names = ArrayList(10)
        values = ArrayList(10)
        formats = ArrayList(10)
        timeRead = ArrayList(10)
    }

    public fun copySetup(str : String) : Boolean {
        resetData()
        val splitLine = str.split(", ")
        var i : Int = 0

        //date = LocalDate.parse(splitLine[i++])
        i++

        while (i+4 <= splitLine.size) {
            names.add(splitLine[i++])
            values.add("")
            i++;
            formats.add(informationFormatStringToEnum(splitLine[i++]))
            timeRead.add(0)
            i++;
            length++
        }
        return true
    }

    public fun fromString(str : String) : Boolean {
        resetData()
        val splitLine = str.split(", ")
        var i : Int = 0

        date = LocalDate.parse(splitLine[i++])

        while (i+4 <= splitLine.size) {
            names.add(splitLine[i++])
            values.add(splitLine[i++])
            formats.add(informationFormatStringToEnum(splitLine[i++]))
            timeRead.add(splitLine[i++].toLong())
            length++
        }
        return true
    }

    public override fun toString() : String {
        var returnStr : StringBuilder = StringBuilder("$date, ")

        for (i in 0 until length) {
            returnStr.append("${names[i]}, ${values[i]}, ${informationFormatEnumToString(formats[i])}, ${timeRead[i]}, ")
        }

        return returnStr.toString()
    }

}