package peter.mitchell.tododaily.HelperClasses

import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import peter.mitchell.tododaily.*
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

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

    enum class RepeatFormat {
        Daily, Weekly, Monthly, Yearly, Never
    }
    public val RepeatFormatStrings : Array<String> = arrayOf(
        "Daily",
        "Weekly",
        "Monthly",
        "Yearly",
        "Never"
    )

    var date : LocalDate = LocalDate.now()
    var length : Int = 0
    var names : ArrayList<String> = ArrayList(10)
    private var values : ArrayList<String> = ArrayList(10)
    var formats : ArrayList<InformationFormat> = ArrayList(10)
    private var timeRead : ArrayList<Long> = ArrayList(10)

    var repeatTime : ArrayList<RepeatFormat> = ArrayList(10)

    public fun addValue(name : String, infoFormat : InformationFormat, repeat : RepeatFormat = RepeatFormat.Daily) : Boolean {
        if (!verifyFormat("", infoFormat)) return false

        names.add(name)
        values.add("")
        formats.add(infoFormat)
        timeRead.add(0)
        repeatTime.add(repeat)
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

    public fun getRawValue(i : Int) : String {
        return values[i]
    }

    public fun getValueIndex(i : Int, name : String, format : InformationFormat) : Int? {

        if ( i >= 0 && i < length && names[i] == name && formats[i] == format ) {
            return i
        }

        var newI = getValueByName(name)
        if (newI != null && formats[newI] == format ) {
            return newI
        }

        return null
    }

    public fun getValueByName(name : String) : Int? {
        for (i in 0 until names.size) {
            if (name == names[i]) {
                return i
            }
        }
        return null
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

    public fun isNumber(format : InformationFormat) : Boolean {
        return format == InformationFormat.integer || format == InformationFormat.onePointDecimal || format == InformationFormat.twoPointDecimal || format == InformationFormat.decimal
    }

    public fun informationFormatStringToEnum(str : String) : InformationFormat {
        var infoFormat : InformationFormat = InformationFormat.text

        if (str == informationFormatStrings[0]) {
            infoFormat = InformationFormat.checkBox
        } else if (str == informationFormatStrings[1]) {
            infoFormat = InformationFormat.integer
        } else if (str == informationFormatStrings[2]) {
            infoFormat = InformationFormat.onePointDecimal
        } else if (str == informationFormatStrings[3]) {
            infoFormat = InformationFormat.twoPointDecimal
        } else if (str == informationFormatStrings[4]) {
            infoFormat = InformationFormat.decimal
        } else if (str == informationFormatStrings[5]) {
            infoFormat = InformationFormat.text
        }

        return infoFormat
    }

    public fun informationFormatEnumToString(infoFormat : InformationFormat) : String {
        var str : String = informationFormatStrings[0]

        if (infoFormat == InformationFormat.checkBox) {
            str = informationFormatStrings[0]
        } else if (infoFormat == InformationFormat.integer) {
            str = informationFormatStrings[1]
        } else if (infoFormat == InformationFormat.onePointDecimal) {
            str = informationFormatStrings[2]
        } else if (infoFormat == InformationFormat.twoPointDecimal) {
            str = informationFormatStrings[3]
        } else if (infoFormat == InformationFormat.decimal) {
            str = informationFormatStrings[4]
        } else if (infoFormat == InformationFormat.text) {
            str = informationFormatStrings[5]
        }

        return str
    }

    public fun repeatTimeEnumToString(repeatFormat: RepeatFormat) : String {
        if (repeatFormat == RepeatFormat.Daily) {
            return RepeatFormatStrings[0]
        } else if (repeatFormat == RepeatFormat.Weekly) {
            return RepeatFormatStrings[1]
        } else if (repeatFormat == RepeatFormat.Monthly) {
            return RepeatFormatStrings[2]
        } else if (repeatFormat == RepeatFormat.Yearly) {
            return RepeatFormatStrings[3]
        } else { //(repeatFormat == RepeatFormat.Never)
            return RepeatFormatStrings[4]
        }
    }

    public fun repeatTimeStringToEnum(str: String) : RepeatFormat {
        if (str == RepeatFormatStrings[0] || str == "") {
            return RepeatFormat.Daily
        } else if (str == RepeatFormatStrings[1]) {
            return RepeatFormat.Weekly
        } else if (str == RepeatFormatStrings[2]) {
            return RepeatFormat.Monthly
        } else if (str == RepeatFormatStrings[3]) {
            return RepeatFormat.Yearly
        } else { // (str == RepeatFormatStrings[4])
            return RepeatFormat.Never
        }
    }

    public fun resetData() {
        date = LocalDate.now()
        length = 0
        names = ArrayList(10)
        values = ArrayList(10)
        formats = ArrayList(10)
        timeRead = ArrayList(10)
        repeatTime = ArrayList(10)
    }

    public fun clearValues(purge : Boolean = false) {
        //values = ArrayList(length+3)
        //timeRead = ArrayList(length+3)
        for (i in 0 until length) {

            if (
                //repeatTime[i] == RepeatFormat.Daily ||
                (repeatTime[i] == RepeatFormat.Weekly && date.dayOfWeek != startOfWeek) ||
                (repeatTime[i] == RepeatFormat.Monthly && date.dayOfMonth != 1) ||
                (repeatTime[i] == RepeatFormat.Yearly && date.dayOfYear != 1) ||
                repeatTime[i] == RepeatFormat.Never
            )
                continue

            values[i] = ("")
            timeRead[i] = 0
        }
    }

    public fun deleteValue(i : Int) {
        names.removeAt(i)
        values.removeAt(i)
        formats.removeAt(i)
        timeRead.removeAt(i)
        repeatTime.removeAt(i)
        length -= 1
    }

    public fun copySetup(str : String) : Boolean {
        resetData()

        fromString(str)

        date = LocalDate.now()
        clearValues()

        return true
    }

    public fun moveFrom(i : Int, to : Int) {

        if (i == to || i >= length || to >= length) return

        var tempName = names[i]
        var tempValue = values[i]
        var tempFormat = formats[i]
        var tempTimeRead = timeRead[i]
        var tempRepeatTime = repeatTime[i]

        if (i < to) {
            for (j in i .. to) {
                if (j < to) {
                    names[j] = names[j+1]
                    values[j] = values[j+1]
                    formats[j] = formats[j+1]
                    timeRead[j] = timeRead[j+1]
                    repeatTime[j] = repeatTime[j+1]
                } else if (j == to) {
                    names[j] = tempName
                    values[j] = tempValue
                    formats[j] = tempFormat
                    timeRead[j] = tempTimeRead
                    repeatTime[j] = tempRepeatTime
                }
            }
        } else if (to < i) {
            for (j in i downTo to) {
                if (j > to) {
                    names[j] = names[j-1]
                    values[j] = values[j-1]
                    formats[j] = formats[j-1]
                    timeRead[j] = timeRead[j-1]
                    repeatTime[j] = repeatTime[j-1]
                } else if (j == to) {
                    names[j] = tempName
                    values[j] = tempValue
                    formats[j] = tempFormat
                    timeRead[j] = tempTimeRead
                    repeatTime[j] = tempRepeatTime
                }
            }
        }

    }

    public fun moveToEnd(i : Int) {
        var tempName = names[i]
        names.removeAt(i)
        var tempValue = values[i]
        values.removeAt(i)
        var tempFormat = formats[i]
        formats.removeAt(i)
        var tempTimeRead = timeRead[i]
        timeRead.removeAt(i)
        var tempRepeatTime = repeatTime[i]
        repeatTime.removeAt(i)

        names.add(tempName)
        values.add(tempValue)
        formats.add(tempFormat)
        timeRead.add(tempTimeRead)
        repeatTime.add(tempRepeatTime)
    }

    public fun importData(activity : Activity, context : Context) : Boolean {

        if (!canExport(activity, context))
            return false

        var importFile = File(importFileName)
        if (!importFile.exists()) {
            Toast.makeText(context, "Could not find import file", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!tempFile.exists()) {
            tempFile.parentFile!!.mkdirs()
            tempFile.createNewFile()
        }
        tempFile.writeText("")

        if (dailyInformationFile.exists()) {
            dailyInformationFile.forEachLine {
                tempFile.appendText(it)
            }
        }

        var malformedFile = false

        importFile.forEachLine {
            if (malformedFile) return@forEachLine

            var tempSaveInformation : SaveInformation = SaveInformation()
            if (!tempSaveInformation.fromString(it)) {
                Toast.makeText(context, "Could not import file, malformed. It MUST be raw data export format", Toast.LENGTH_SHORT).show()
                malformedFile = true
                return@forEachLine
            }

            if (!tempFile2.exists()) {
                tempFile2.parentFile!!.mkdirs()
                tempFile2.createNewFile()
            }
            tempFile2.writeText("")

            var currentWritten : Boolean = false;
            tempFile.forEachLine {

                if (!currentWritten) {
                    var lineDate: LocalDate = LocalDate.parse(it.split(",")[0])

                    if (tempSaveInformation.date > lineDate) {
                        tempFile2.appendText(tempSaveInformation.toString() + "\n")
                        tempFile2.appendText(it + "\n")
                        currentWritten = true
                    } else if (tempSaveInformation.date == lineDate) {
                        var tempSaveInformation2 : SaveInformation = SaveInformation()
                        if (!tempSaveInformation2.fromString(it)) {
                            malformedFile = true
                            Toast.makeText(context, "Unknown error.", Toast.LENGTH_SHORT).show()
                            return@forEachLine
                        }

                        for (i in 0 until tempSaveInformation.length) {
                            if (!tempSaveInformation2.names.contains(tempSaveInformation.names[i])) {
                                if (!tempSaveInformation2.addValue(
                                    tempSaveInformation.names[i],
                                    tempSaveInformation.formats[i],
                                    tempSaveInformation.repeatTime[i],
                                )) {
                                    Toast.makeText(context, "Could not import file, malformed. value contains wrong format", Toast.LENGTH_SHORT).show()
                                    malformedFile = true
                                    return@forEachLine
                                }
                            }
                        }

                        tempFile2.appendText(tempSaveInformation2.toString() + "\n")
                        currentWritten = true
                    } else {
                        tempFile2.appendText(it + "\n")
                    }

                } else {
                    tempFile2.appendText(it + "\n")
                }

            }

            if (!currentWritten) {
                tempFile2.appendText(tempSaveInformation.toString() + "\n")
            }

            if (!tempFile.delete()) {
                Log.e("tdd-Importing: ", "Failed to save, permission denied")
                Toast.makeText(context, "Could not import file, could not delete a temporary file", Toast.LENGTH_SHORT).show()
                tempFile2.delete()
                malformedFile = true
                return@forEachLine
            }

            tempFile2.renameTo(tempFile)
        }

        if (malformedFile) {
            tempFile.delete()
            return false
        }

        // rename the temp file to dailyInformation file and return true
        tempFile.renameTo(dailyInformationFile)

        return true
    }

    public fun fromString(str : String) : Boolean {
        resetData()
        Log.i("SaveInformation.fromString", str)

        var i : Int = 0
        var j : Int = 0
        var readingString = false
        var currentString = StringBuilder()

        while (i < str.length) {

            if (str[i] != ',' || readingString) {

                if (str[i] == '\"' && currentString.isEmpty() && i+1 < str.length) {
                    i++
                    readingString = true
                }

                if (str[i] == '\"' && i+1 < str.length && str[i+1] == ',') {
                    readingString = false
                } else {
                    currentString.append(str[i])
                }

            } else {


                if (j == 0) {
                    date = LocalDate.parse(currentString.toString())
                } else if (j%5 == 1) {
                    names.add(currentString.toString())
                } else if (j%5 == 2) {

                    /*if (currentString.length > 1 && currentString[0] == '\"' && currentString[currentString.length-1] == '\"') {
                        currentString.removeRange(0,1)
                        currentString.removeRange(currentString.length-1,currentString.length)
                    }*/

                    values.add(currentString.toString().replace("\"\"", "\""))

                } else if (j%5 == 3) {

                    var splitString = currentString.toString().split("-")
                    if (splitString.size > 1) {
                        formats.add(informationFormatStringToEnum(splitString[0]))
                        repeatTime.add(repeatTimeStringToEnum(splitString[1]))
                    } else {
                        repeatTime.add(repeatTimeStringToEnum(""))
                        formats.add(informationFormatStringToEnum(currentString.toString()))
                    }

                } else if (j%5 == 4) {
                    timeRead.add(currentString.toString().toLong())
                    j++
                    length++
                }
                j++
                currentString.clear()
            }

            i++
        }


        /*val splitLine = str.split(",")


        date = LocalDate.parse(splitLine[i++])

        while (i+4 <= splitLine.size) {
            names.add(splitLine[i++])
            values.add(splitLine[i++])
            formats.add(informationFormatStringToEnum(splitLine[i++]))
            timeRead.add(splitLine[i++].toLong())
            length++
        }*/
        return true
    }

    public override fun toString() : String {
        var returnStr : StringBuilder = StringBuilder("$date,")

        for (i in 0 until length) {
            if (formats[i] == InformationFormat.text) {
                returnStr.append("${names[i]},\"${values[i].replace("\"","\"\"").replace("\n"," ")}\",${informationFormatEnumToString(formats[i])}-${repeatTimeEnumToString(repeatTime[i])},${timeRead[i]},")
            } else {
                returnStr.append("${names[i]},${values[i]},${informationFormatEnumToString(formats[i])}-${repeatTimeEnumToString(repeatTime[i])},${timeRead[i]},")
            }
        }

        return returnStr.toString()
    }

    class ValueInfo(valueName : String, valueFormat : InformationFormat?) {
        val name : String = valueName
        val format : InformationFormat? = valueFormat
    }

    /** Loops through daily information file setting up colInfo. This is used because you cannot re-
     * write the start of a file once you have written the rest, meaning the column labels must be
     * created before the rest can be written. I believe this is the most efficient way.
     *
     * @return the setup colInfo for the export custom order
     */
    public fun setupColInfoForOrder() : ArrayList<ValueInfo> {
        if (!dailyInformationFile.exists()) return ArrayList()
        val colInfo = ArrayList<ValueInfo>()
        val tempSaveInformation : SaveInformation = SaveInformation()

        // for each line in the file, check if it exists in the colInfo, add it if it doesn't
        dailyInformationFile.forEachLine {
            tempSaveInformation.fromString(it)

            for (i in 0 until tempSaveInformation.length) {
                var containsName = false
                for (j in 0 until colInfo.size) {
                    if (tempSaveInformation.names[i] == colInfo[j].name) {
                        containsName = true
                        break
                    }
                }
                if (!containsName)
                    colInfo.add(ValueInfo(tempSaveInformation.names[i], tempSaveInformation.formats[i]))
            }

        }

        return colInfo
    }

    public fun exportToOrderByNames(strFormat : String, colInfo : ArrayList<ValueInfo>) : String {

        // remove invalid characters in strFormat
        val validChars = arrayOf('n', 'v', 'i', 't', ',', )
        var remIndex = 0
        while (remIndex < strFormat.length) {
            if (!validChars.contains(strFormat[remIndex]))
                strFormat.removeRange(remIndex, remIndex+1)
            else
                remIndex++
        }

        // update the colinfo, loop through current and check what colinfo is missing
        /*for (i in 0 until length) {
            var containsName = false
            for (j in 0 until colInfo.size) {
                if (names[i] == colInfo[j].name) {
                    containsName = true
                    break
                }
            }
            if (!containsName)
                colInfo.add(ValueInfo(names[i], formats[i]))
        }*/

        // loop through colInfo and add the ones listed

        var returnStr : StringBuilder = StringBuilder()
        returnStr.append("$date,")

        for (i in 0 until colInfo.size) {

            var newI = getValueIndex(i, colInfo[i].name, colInfo[i].format!!)

            if (newI != null) {

                for (j in strFormat.indices) {
                    if (strFormat[j] == 'n') {
                        returnStr.append("${names[newI]},")
                    } else if (strFormat[j] == 'v') {
                        if (formats[newI] == InformationFormat.text) {
                            returnStr.append("\"${values[newI].replace("\"","\"\"").replace("\n"," ")}\",")
                        } else {
                            returnStr.append("${values[newI]},")
                        }
                    } else if (strFormat[j] == 'i') {
                        returnStr.append("${informationFormatEnumToString(formats[newI])}-${repeatTimeEnumToString(repeatTime[newI])},")
                    } else if (strFormat[j] == 't') {
                        returnStr.append("${timeRead[newI]},")
                    } else if (strFormat[j] == ',') {
                        returnStr.append(',')
                    }
                }

            } else {
                for (j in strFormat.indices) {
                    returnStr.append(',')
                }
            }

        }

        return returnStr.toString()

    }

    public fun exportToCustomByNames(strFormat : String, colInfo : ArrayList<ValueInfo>) : String {
        var returnStr = StringBuilder()

        // update the colinfo only if colInfo is EMPTY (first line)
        if (colInfo.size == 0) {
            if (length == 0)
                return ""

            returnStr.append("Date,")

            /*for (i in 0 until length) {
                colOffsets.add(-1)
            }*/

            var j = 0
            while (j < strFormat.length) {

                var strRead : Char = ' '
                var i : Int = -1

                if (strFormat[j] == 'n' || strFormat[j] == 'v' || strFormat[j] == 'i' || strFormat[j] == 't') {
                    i = 0
                    strRead = strFormat[j]
                    var currentIndex = j+1
                    while (currentIndex < strFormat.length && strFormat[currentIndex].isDigit()) {
                        try {
                            i *= 10
                            i += strFormat[currentIndex].digitToInt()
                        } catch (e: NumberFormatException) {
                            i /= 10
                            break
                        }
                        currentIndex++
                    }
                    j = currentIndex-1
                    i -= 1
                }

                if (strFormat[j] == ',') {
                    returnStr.append(',')
                    colInfo.add(ValueInfo(",", null))
                } else if (i == -1) {
                    // do nothing
                } else if (strRead == 'n') {
                    returnStr.append("${names[i]} Name,")
                    colInfo.add(ValueInfo(names[i], formats[i]))
                    //colOffsets[i] = colInfo.size-1
                } else if (strRead == 'v') {
                    returnStr.append("${names[i]},")
                    colInfo.add(ValueInfo(names[i], formats[i]))
                } else if (strRead == 'i') {
                    returnStr.append("${names[i]} Save Format,")
                    colInfo.add(ValueInfo(names[i], formats[i]))
                } else if (strRead == 't') {
                    returnStr.append("${names[i]} Time Read,")
                    colInfo.add(ValueInfo(names[i], formats[i]))
                }

                j++
            }

            returnStr.append("\n")

            /*for (i in length-1 downTo 0) {
                if (colOffsets[i] == -1)
                    colOffsets.removeAt(i)
            }*/
        }

        // loop through colInfo and add the ones listed
        returnStr.append("$date,")

        var j = 0
        for (i in 0 until colInfo.size) {

            if (colInfo[i].format == null) {
                returnStr.append(colInfo[i].name)
            } else {
                val newI = getValueIndex(-1, colInfo[i].name, colInfo[i].format!!)
                while (j < strFormat.length && !(strFormat[j] == 'n' || strFormat[j] == 'v' ||
                        strFormat[j] == 'i' || strFormat[j] == 't')
                ) {
                    j++
                }

                if (j == strFormat.length) {
                    Log.e("SaveInformation.exportToCustomByNames", "Unknown error, j exceeded length")
                    return ""
                }

                if (newI == null) {
                    returnStr.append(",")
                } else if (strFormat[j] == 'n') {
                    returnStr.append("${names[newI]},")
                } else if (strFormat[j] == 'v') {
                    if (formats[newI] == InformationFormat.text) {
                        returnStr.append("\"${values[newI].replace("\"","\"\"").replace("\n"," ")}\",")
                    } else {
                        returnStr.append("${values[newI]},")
                    }
                } else if (strFormat[j] == 'i') {
                    returnStr.append("${informationFormatEnumToString(formats[newI])}-${repeatTimeEnumToString(repeatTime[newI])},")
                } else if (strFormat[j] == 't') {
                    returnStr.append("${timeRead[newI]},")
                }

            }

        }

        return returnStr.toString()
    }

    public fun exportToCustomOrder(strFormat : String, addColLabel : Boolean = false) : String {
        var returnStr : StringBuilder = StringBuilder()

        var doneIndexes = ArrayList<Int>()

        if (addColLabel) {

            returnStr.append("Date,")

            for (i in 0 until length) {
                for (j in strFormat.indices) {

                    if (strFormat[j] == 'n') {
                        returnStr.append("${names[i]} Name,")
                    } else if (strFormat[j] == 'v') {
                        returnStr.append("${names[i]},")
                    } else if (strFormat[j] == 'i') {
                        returnStr.append("${names[i]} Save Format,")
                    } else if (strFormat[j] == 't') {
                        returnStr.append("${names[i]} Time Read,")
                    } else if (strFormat[j] == ',') {
                        returnStr.append(',')
                    }

                }
            }
            returnStr.append("\n")
        }

        returnStr.append("$date,")

        for (i in 0 until length) {

            for (j in strFormat.indices) {
                if (strFormat[j] == 'n') {
                    returnStr.append("${names[i]},")
                } else if (strFormat[j] == 'v') {
                    if (formats[i] == InformationFormat.text) {
                        returnStr.append("\"${values[i].replace("\"","\"\"").replace("\n"," ")}\",")
                    } else {
                        returnStr.append("${values[i]},")
                    }
                } else if (strFormat[j] == 'i') {
                    returnStr.append("${informationFormatEnumToString(formats[i])}-${repeatTimeEnumToString(repeatTime[i])},")
                } else if (strFormat[j] == 't') {
                    returnStr.append("${timeRead[i]},")
                } else if (strFormat[j] == ',') {
                    returnStr.append(',')
                }
            }
        }

        return returnStr.toString()
    }

    public fun exportToCustomString(strFormat: String, addColLabel : Boolean = false) : String? {
        var returnStr : StringBuilder = StringBuilder()

        if (addColLabel) {

            returnStr.append("Date,")

            var j = 0
            while (j < strFormat.length) {

                var strRead : Char = ' '
                var i : Int = -1

                if (strFormat[j] == 'n' || strFormat[j] == 'v' || strFormat[j] == 'i' || strFormat[j] == 't') {
                    i = 0
                    strRead = strFormat[j]
                    var currentIndex = j+1
                    while (currentIndex < strFormat.length && strFormat[currentIndex].isDigit()) {
                        try {
                            i *= 10
                            i += strFormat[currentIndex].digitToInt()
                        } catch (e: NumberFormatException) {
                            i /= 10
                            break
                        }
                        currentIndex++
                    }
                    j = currentIndex-1
                    i -= 1
                }

                if (strFormat[j] == ',') {
                    returnStr.append(',')
                } else if (i == -1) {
                    // do nothing
                } else if (strRead == 'n') {
                    returnStr.append("${names[i]} Name,")
                } else if (strRead == 'v') {
                    returnStr.append("${names[i]},")
                } else if (strRead == 'i') {
                    returnStr.append("${names[i]} Save Format,")
                } else if (strRead == 't') {
                    returnStr.append("${names[i]} Time Read,")
                }

                j++
            }

            returnStr.append("\n")
        }

        returnStr.append("$date,")

        var j = 0
        while (j < strFormat.length) {

            var strRead : Char = ' '
            var i : Int = -1

            if (strFormat[j] == 'n' || strFormat[j] == 'v' || strFormat[j] == 'i' || strFormat[j] == 't') {
                i = 0
                strRead = strFormat[j]
                var currentIndex = j+1
                while (currentIndex < strFormat.length && strFormat[currentIndex].isDigit()) {
                    try {
                        i *= 10
                        i += strFormat[currentIndex].digitToInt()
                    } catch (e: NumberFormatException) {
                        i /= 10
                        break
                    }
                    currentIndex++
                }
                j = currentIndex-1
                i -= 1
            }

            if (i < 0) {
                returnStr.append(strFormat[j])
            } else {
                if (i >= length) {
                    // do nothing
                } else if (strRead == 'n') {
                    returnStr.append("${names[i]},")
                } else if (strRead == 'v') {
                    if (formats[i] == InformationFormat.text) {
                        returnStr.append("\"${values[i].replace("\"","\"\"").replace("\n"," ")}\",")
                    } else {
                        returnStr.append("${values[i]},")
                    }
                } else if (strRead == 'i') {
                    returnStr.append("${informationFormatEnumToString(formats[i])}-${repeatTimeEnumToString(repeatTime[i])},")
                } else if (strRead == 't') {
                    returnStr.append("${timeRead[i]},")
                }
            }

            j++
        }

        return returnStr.toString()
    }
}