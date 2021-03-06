package com.github.vok.example.crud.personeditor

import com.github.vok.framework.sql2o.vaadin.dataProvider
import com.github.vok.framework.sql2o.vaadin.generateFilterComponents
import com.github.vok.framework.sql2o.vaadin.withFilter
import com.github.vok.karibudsl.*
import com.github.vok.karibudsl.ModifierKey.Alt
import com.github.vok.karibudsl.ModifierKey.Ctrl
import com.github.vokorm.db
import com.github.vokorm.deleteById
import com.vaadin.event.ShortcutAction.KeyCode.C
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.ui.Button
import com.vaadin.ui.Grid
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.renderers.ButtonRenderer
import com.vaadin.ui.renderers.LocalDateRenderer
import java.util.*

/**
 * Demonstrates a CRUD over [Person]. Note how the autoViewProvider automatically discovers your view and assigns a name to it.
 */
@AutoView
class CrudView: VerticalLayout(), View {
    override fun enter(event: ViewChangeListener.ViewChangeEvent) {
        personGrid.refresh()
    }

    companion object {
        fun navigateTo() = navigateToView<CrudView>()
    }

    private lateinit var createButton: Button
    private val personGrid: Grid<Person>

    init {
        setSizeFull(); isMargin = false
        horizontalLayout {
            createButton = button("Create New Person (Ctrl+Alt+C)") {
                onLeftClick { createOrEditPerson(Person(created = Date())) }
                clickShortcut = Ctrl + Alt + C
            }
            button("Generate testing data", { generateTestingData() })
        }
        // the SQL2O list demo - shows all instances of a particular database table, allows sorting and filtering.
        // you can restrict the values by writing the following expression:
        // dataProvider = Person.dataProvider.withFilter { Person::age between 20..60 }
        // any user-configured filters will be applied on top of this filter.
        personGrid = grid(Person::class, dataProvider = Person.dataProvider ) {
            expandRatio = 1f; setSizeFull()

            // example of a custom renderer which converts value to a displayable string.
            // @todo mavi it is better to employ value provider for this; yet value provider cannot be changed
            column(Person::created) {
                setRenderer(ConvertingRenderer<Date?>({ it!!.toInstant().toString() }))
            }

            // show these columns, and in this order
            showColumns(Person::id, Person::name, Person::age, Person::dateOfBirth, Person::maritalStatus, Person::alive, Person::created)

            // a sample of how to reconfigure a column
            column(Person::id) {
                isSortable = false
            }
            column(Person::dateOfBirth) {
                setRenderer(LocalDateRenderer())
            }

            // add additional columns with buttons
            addColumn({ "Show" }, ButtonRenderer<Person>({ event -> PersonView.navigateTo(event.item) }))
            addColumn({ "Edit" }, ButtonRenderer<Person>({ event -> createOrEditPerson(event.item) })).id = "edit"
            addColumn({ "Delete" }, ButtonRenderer<Person>({ event -> deletePerson(event.item.id!!) }))

            // automatically create filters, based on the types of values present in particular columns.
            appendHeaderRow().generateFilterComponents(this, Person::class)
        }
    }

    private fun generateTestingData() {
        db {
            (0..85).forEach {
                Person(name = "generated$it", age = it + 15, maritalStatus = MaritalStatus.Single, alive = true, created = Date()).save()
            }
        }
        personGrid.refresh()
    }

    private fun deletePerson(id: Long) {
        Person.deleteById(id)
        personGrid.refresh()
    }

    private fun createOrEditPerson(person: Person) {
        CreateEditPerson(person).apply {
            addCloseListener { personGrid.refresh() }
            UI.getCurrent().addWindow(this)
        }
    }
}
