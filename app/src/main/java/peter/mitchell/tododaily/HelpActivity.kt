package peter.mitchell.tododaily

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.help_screen.*

class HelpActivity : AppCompatActivity() {

    val appString : String =
"""
Creator: Peter Mitchell - tododaily55@gmail.com

Main Information:
	"TodoDaily" was created as a way for me to track daily information and remind me of daily events with notifications, as I could not find an app fitting the description that worked for my uses. In development, I also decided to add the ability to store a to-do list and notes in the app, as they are simple additions that I find helpful to have all in one place. These sections can be disabled if desired. 
	
Description of Each Section:
	The "Home" page is intended to help the user track daily information to be exported to a spreadsheet later.
	The "Todo" page provides an easy way to create and remove simple text items, intended to be used as a to-do list.
	The "Notes" page simply provides the functionality of a basic notes app.
	The "Notifs" page allows the user to create one-time and daily notifications which are displayed at the given times, allowing an alarm-like functionality without the loud alarm that needs to be disabled.
	Any section except the first can be toggled on or off in the settings. 

The "T" Button at the Top (Quick Timer):
	The "T" button, or "Main Quick Timer" button, allows the user to create a quick timer with a set offset from the current time, without navigating to the "Notifications" page. The offset to this notification can be set in the settings page. This button is not shown if the "Notifications" section is disabled, as it requires its functionality. (more details under the notifications help)
""".trimIndent()

    val homeString : String =
"""
Main Information:
	The "Home" page is intended as a way of helping the user remember and track the tasks they wish to do every day. The values can be recorded using checkboxes, numerical inputs, or text inputs. For each value, both the value and the time recorded are saved. The values are sorted into daily, weekly, monthly, yearly, and never repeating. The values will keep their values from the day recorded until the time has elapsed. For example, the first of every month for monthly values. (Weekly will refresh on whatever day you set as the "start of the week".) 
	Past dates can be viewed by clicking the pick date button, then selecting the date from the drop-down menu. Data can be exported by pressing the export button, which is then saved to your downloads folder. More complicated export options are available in the "MANAGE" menu, whereas the main export button uses your default. Data can also be viewed using the view value option just above the "+" button. Simply select a value using the drop-down menu and press "Show", it will display all the recorded values under that name and their dates.

Adding New Information:
	New information to track can be added at the bottom of the page using the "+" button, allowing you to specify the name, type of information, and how often it should be done. The item will then be added to the respective list. (Adding new titles DOES NOT add them to past dates.)

The "MANAGE" page:
	The Home page management options are displayed by pressing the "MANAGE" button in the top right. This page provides the options to delete data, rearrange data, add dates that have passed, and export the data with better control. Editing data will only change the date selected when the manage page was opened.
	"Delete Titles" allows the user to click a title, which will be deleted from the currently selected date (after confirmation). 
	"Rearrange" allows the user to click a title, and provide a new position for it.
	"Dates" allows the user to delete dates from memory, with the option to delete all dates preceding the date selected for ease of use.
	"Add Date" allows the user to add a date that was not recorded (e.g. because they did not record anything on that day.)
	Export Options provides many ways to adjust how the data is exported.
	The "Include Label Line" check box, when enabled, will add a line at the top of the export file that labels the latest titles.
	For the order line, the letters n (name), v (value), i (information format), and t (time of entry) can be added (multiple times or not at all if desired) in any order, and the data will be exported in the given order.
	The "Custom" text box allows the user to specify more precisely what should be exported. Instead of the data being repeated for each value, like the order line, each will only be printed if specifically stated, and a number must be provided which represents the value number. 
""".trimIndent()



    val todoString : String =
"""
Main Information:
	The "Todo" page is intended to be used as a to-do list, allowing the user to create, remove, and sort items that need to be done. "Sections" can be added by pressing the "+" button at the bottom of the list. These sections store the main todo elements and allow the user to sort their todo elements. To sort or edit the sections, simply click the section title after it has been created. Todo elements can be created by pressing the "+" button beside any of the section titles. To edit a todo element, press on the text of the element you wish to edit. When creating or editing an element the user can select the section to store it in, the position in that section, and the title of the element using the drop-downs and text box provided.
""".trimIndent()



    val notesString : String =
"""
Main Information:
	The "Notes" page gives the user the ability to store blocks of text under titles, in either the notes or lists section. Currently, there is no difference between notes and lists, and the sections simply allow for minor sorting. To create a note/list, simply click the "+" button beside the section you would like to create the note in; you will then be redirected to the "Edit Note" page. This page contains two edit text boxes; the first of which is the title input, the second being the main text to be saved. The title text cannot match any other notes you have created, use any invalid characters (<, >, :, |, etc), or be an invalid name in any common operating system. If it is, an error will show when attempting to save. 
	The note is not saved periodically and will be lost if it is not saved before clearing from memory. Pressing the back button will attempt to save the note, but is not recommended as if it fails to save, it will still exit the note. Using the provided back button will only exit the note if it successfully saves. The delete button will simply delete the note from memory and exit to the "Notes" page. (It will still ask for confirmation.)

The "MANAGE" page:
	The Manage page has 4 sections: rearrange notes, rearrange lists, export notes, and export lists. Each section does as described, by pressing the title of the corresponding note/list, the application does the described action. For rearranging, pressing the title of the element will provide a dialogue for the user to input the index they would like the note to be moved to in its section. For exporting, pressing on the element will provide a dialogue to confirm the action. If confirmed, the selected note/list will be exported to the downloads folder (the same location as the daily information export) but will be given the same name as the note. When exporting, just as the daily information export does, if it already exists it will increment the name of the export to not overwrite existing files (e.g. "note1" -> "note1(1)"). Lastly at the bottom of the page is an "EXPORT ALL" button, allowing the user to export all notes and lists at once as if they had exported them all individually.
""".trimIndent()



    val notifsString : String =
"""
Main Information:
	The "Notifications" page allows the user to schedule notifications, allowing them to be repeated daily if desired. These can be reminders, events, etc, allowing for reminders without using the alarm app, delivering with just the notification sound and not needing to turn them off on the delivery. There are three types of notifications that can be scheduled: "One Time Notifications", "Daily Notifications", and "System Notifications". Although they are all delivered the same way in the notification, they are sorted into three sections and have differences in how they are scheduled and held. One Time Notifications are only sent once, and immediately after delivery are deleted from memory. Daily Notifications are sent once per day at the selected time and are not given a date as they are sent every day. System Notifications are the same as One Time Notifications but are created either by snoozing a notification or using a quick timer. The time of the next notification to be shown is always displayed at the top of the page.

Creating/editing a notification:
	The primary way to create notifications is by pressing the "NEW" button beside the section you would like it to be under. You will then be redirected to the "Edit Notification" page. This page allows you to input all the information for the notification, when it should be sent, and where in your list of notifications it should be. If the "Repeat Daily?" check box is not selected, you must also provide the date of the notification. This is also how you edit an existing notification. By pressing on an existing notification on the "Notifications" page you will be redirected there. Pressing the Android back button will not save any data, pressing the "DELETE" button will delete the notification from memory if it exists, and pressing "SUBMIT" will save the notification to memory or update the existing notification. Two notifications can never be scheduled for the same time of day.

Quick notifications and snoozing:
	Notifications can also be created using quick timers which will schedule a notification at the labelled time (e.g. "5" will schedule a notification for 5 minutes from the time of the button press). These notifications will be saved under system notifications, and are not given an automatic name and description.
	Notifications, after being sent, can also be snoozed by pressing the "Snooze" button shown on the notification. Pressing the snooze button will not change the existing notification, but schedule a new System Notification as if you had pressed a quick timer. The only difference is it will be given the same name as the original notification with "-Snoozed" appended to it (e.g. "MyNotificaionTitle-Snoozed") and the time it will be set for after "Snooze time" instead of a set number. (This can be configured in the settings.)
""".trimIndent()





    var selectedHelpFragment : fragments? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.help_screen)

        if (darkMode)
            mainBackground.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundDark))
        else
            mainBackground.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundLight))


        //oneTimeNotification = intent.getBooleanExtra("oneTimeNotification", false)

        appHelpButton.setOnClickListener {
            selectedHelpFragment = null
            reloadText()
        }
        homeHelpButton.setOnClickListener {
            selectedHelpFragment = fragments.home
            reloadText()
        }
        todoHelpButton.setOnClickListener {
            selectedHelpFragment = fragments.todo
            reloadText()
        }
        notesHelpButton.setOnClickListener {
            selectedHelpFragment = fragments.notes
            reloadText()
        }
        notifsHelpButton.setOnClickListener {
            selectedHelpFragment = fragments.notifs
            reloadText()
        }

        reloadText()
    }

    /** Reloads the text to be displayed */
    fun reloadText() {
        if (selectedHelpFragment == null) {
            mainInformation.setText(appString)
        } else if (selectedHelpFragment == fragments.home) {
            mainInformation.setText(homeString)
        } else if (selectedHelpFragment == fragments.todo) {
            mainInformation.setText(todoString)
        } else if (selectedHelpFragment == fragments.notes) {
            mainInformation.setText(notesString)
        } else if (selectedHelpFragment == fragments.notifs) {
            mainInformation.setText(notifsString)
        }
    }

}