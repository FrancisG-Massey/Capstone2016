/*******************************************************************************
 * Copyright (C) 2016, Nest NZ
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.nestnz.app.views;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.model.CatchType;

import com.gluonhq.charm.glisten.control.Dialog;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * 
 */
public class CatchSelectionDialog extends Dialog<CatchType> {

    private static final Logger LOG = Logger.getLogger(CatchSelectionDialog.class.getName());
    
    private final ObservableList<CatchType> catchTypes = FXCollections.observableArrayList();

	public CatchSelectionDialog () {
		super(true);//Make this a full screen dialog
		
		GridPane controls = new GridPane();		
    	setContent(controls);
    	setTitleText("Select Catch");
    	
    	ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        controls.getColumnConstraints().addAll(column1, column2);
    	
    	Button empty = makeOptionButton(0);
    	Button option2 = makeOptionButton(1);
    	
    	Button other = new Button("Other");
    	other.setMaxSize(1000, 1000);
    	other.getStyleClass().add("large-button");
    	GridPane.setConstraints(other, 0, 1, 2, 1);//Set as center cell (spans both rows)

    	Button option3 = makeOptionButton(2);
    	Button option4 = makeOptionButton(3);
    	
    	controls.getChildren().addAll(empty, option2, option3, option4, other);
	}
	
	public ObservableList<CatchType> getCatchTypes () {
		return catchTypes;
	}
    
    /**
     * Constructs one of the selection buttons for the catch selection screen.
     * Binds the catch type displayed on this button to {@link CatchType#EMPTY} if place is 0, or to one of the top three catches for the trapline if place is between 1 and 3.
     * @param place The button placement (ranges from 0 to 3)
     * @return The catch selection button
     */
    private Button makeOptionButton (int place) {
    	ObjectBinding<CatchType> catchType;
    	if (place == 0) {
    		catchType = Bindings.createObjectBinding(() -> CatchType.EMPTY);
    	} else {
    		catchType = Bindings.valueAt(catchTypes, place-1);/*Bindings.createObjectBinding(() -> {
    			CatchType t;
    			if (traplineProperty.get() == null) {
    				t =  CatchType.EMPTY;
    			} else if (traplineProperty.get().getCatchTypes().size() < place) {
    	    		LOG.log(Level.WARNING, "Trapline lacks a catch type entry at place "+place+" (only "+traplineProperty.get().getCatchTypes().size()+" available)");
    				t = CatchType.EMPTY;//TODO: Currently puts another 'empty' entry down if nothing is specified. Should something else be put instead?
    			} else {
    				t = traplineProperty.get().getCatchTypes().get(place-1);
    			}    			
    			return Objects.requireNonNull(t, "Invalid catch type at "+place);//Make sure we don't have a 'null' catch type anywhere 
    		}, traplineProperty);*/
    	}
    	Button button = new Button();
    	button.getStyleClass().add("catch-select-option");
    	button.textProperty().bind(Bindings.createStringBinding(() -> catchType.get() == null ? "..." : catchType.get().getName(), catchType));
    	button.setMaxSize(1000, 1000);
    	//button.getStyleClass().add("large-button");
    	GridPane.setConstraints(button, place % 2, place > 1 ? 2 : 0);
    	GridPane.setHgrow(button, Priority.ALWAYS);
    	GridPane.setVgrow(button, Priority.ALWAYS);
    	button.setOnAction(evt -> {
    		LOG.log(Level.FINE, "Selected catch: "+catchType.get());
    		setResult(catchType.get());
    		hide();
    	});
    	return button;
    }
}
