package eme.ui;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;

import eme.model.ExtractedElement;
import eme.model.IntermediateModel;

/**
 * A listener which is notified of changes to the checked state of items in check box viewers. Updates the selected
 * state of the correlating intermediate model element.
 * @author Timur Saglam
 */
public class CheckStateListener implements ICheckStateListener {
    private IntermediateModel model;

    /**
     * Basic constructor, creates the listener.
     * @param model is the intermediate model which whose root element shall be excluded from the listeners
     * functionality.
     */
    public CheckStateListener(IntermediateModel model) {
        this.model = model;
    }

    @Override
    public void checkStateChanged(CheckStateChangedEvent event) {
        if (event.getElement() instanceof ExtractedElement) { // if is intermediate model element
            updateSelection((ExtractedElement) event.getElement(), event);
        }
    }

    /**
     * Updates the selection of the intermediate model according to a {@link CheckStateChangedEvent}, if the checked
     * element is not the root package.
     */
    private void updateSelection(ExtractedElement element, CheckStateChangedEvent event) {
        if (model.getRoot().equals(element)) { // if is root package
            event.getCheckable().setChecked(element, true); // check the check box again
        } else { // if is not the root package
            element.setSelected(event.getChecked()); // set selected if checked and vice versa
        }
    }

}
