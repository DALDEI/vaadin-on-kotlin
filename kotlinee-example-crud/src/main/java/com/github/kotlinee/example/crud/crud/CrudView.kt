package com.github.kotlinee.example.crud.crud

import com.github.kotlinee.example.crud.crud.Person
import com.github.kotlinee.framework.*
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.ui.Button
import com.vaadin.ui.Grid
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.renderers.ClickableRenderer

/**
 * Demonstrates a CRUD over [Person]. Note how the autoViewProvider automatically discovers your view and assigns a name to it.
 */
class CrudView: VerticalLayout(), View {
    override fun enter(event: ViewChangeListener.ViewChangeEvent?) {
        refreshGrid()
    }

    companion object {
        fun navigateTo() = navigateToView(CrudView::class.java)
    }

    private val createButton: Button
    private val personGrid: Grid
    private val personGridDS = jpaContainer<Person>()

    init {
        setSizeFull()
        createButton = button("Create New Person") {
            addClickListener { createOrEditPerson(Person()) }
        }
        // the JPA list demo - shows all instances of a particular JPA entity, allow sorting. @todo filtering
        personGrid = grid(dataSource = personGridDS) {
            expandRatio = 1f
            addButtonColumn("edit", "Edit", ClickableRenderer.RendererClickListener {
                db { createOrEditPerson(em.get(it.itemId)) }
            })
            addButtonColumn("delete", "Delete", ClickableRenderer.RendererClickListener {
                db { em.deleteById<Person>(it.itemId) }
                refreshGrid()
            })
            setColumns("id", "name", "age", "edit", "delete")
            setSizeFull()
        }
    }

    private fun createOrEditPerson(person: Person) {
        CreateEditPerson(person).apply {
            addCloseListener({ refreshGrid() })
            UI.getCurrent().addWindow(this)
        }
    }

    private fun refreshGrid() {
        personGridDS.refresh()
    }
}