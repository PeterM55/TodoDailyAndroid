package peter.mitchell.tododaily.ui.home

import android.text.InputType

class SaveInformation {

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

    var length : Int = 0
    var names : ArrayList<String> = ArrayList(10)
    var values : ArrayList<String> = ArrayList(10)
    var formats : ArrayList<InformationFormat> = ArrayList(10)

    public fun addValue(name : String, value : String, infoFormat : InformationFormat) : Boolean {
        if (!verifyFormat(value, infoFormat)) return false

        names.add(name)
        values.add(value)
        formats.add(infoFormat)
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

    public fun getValue(i : Int) : String {
        if (values[i] == "") {
            return "---"
        }
        return values[i]
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

}