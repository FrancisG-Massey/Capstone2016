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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.model.CatchType;

import com.gluonhq.charm.glisten.control.Dialog;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionModel;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * 
 */
public class CatchSelectionDialog extends Dialog<CatchType> {

    private static final Logger LOG = Logger.getLogger(CatchSelectionDialog.class.getName());
    
    private final ObservableList<CatchType> catchTypes = FXCollections.observableArrayList();
    
    private final ListView<CatchType> fullSelectionList = new ListView<>(catchTypes);
    
    /**
     * A button used only to get the css background styles for catch type buttons
     */
    private final Button dummyButton = new Button();
    
    /**
     * A button used only to get the css hover background styles for catch type buttons
     */
    private final Button dummyHoverButton = new Button();
    
    /**
     * A css {@link PseudoClass} representing a catch selection button containing an image
     */
    private final PseudoClass imageButtonClass = PseudoClass.getPseudoClass("image");

	public CatchSelectionDialog () {
		super(true);//Make this a full screen dialog
		
		fullSelectionList.setId("catch-select-list");
		fullSelectionList.setCellFactory(list -> new ListCell<CatchType>() {
			@Override
			protected void updateItem(CatchType item, boolean empty) {
				super.updateItem(item, empty);
				if (!empty && item != null) {
					setText(item.getName());
					if (item.getImageUrl() != null) {
						ImageView imageView = new ImageView(item.getImage());
						imageView.setPreserveRatio(true);
						imageView.setFitHeight(100);
						setGraphic(imageView);
					}
				} else {
					setText(null);
					setGraphic(null);
				}
			}
		});
		SelectionModel<CatchType> selectionModel = fullSelectionList.getSelectionModel();
		selectionModel.selectedItemProperty().addListener((obs, oldItem, newItem) -> {
			if (newItem != null) {
				LOG.log(Level.FINE, "Selected catch: "+newItem);
	    		setResult(newItem);
	    		hide();
			}
		});
		
		
		//Since setContent() doesn't dynamically update content, we need to use a wrapper layout and call setCenter() to change the controls while the dialog is open
		BorderPane wrapper = new BorderPane();
    	setContent(wrapper);
		
		GridPane controls = new GridPane();
		wrapper.setCenter(controls);
		
    	//Whenever the dialog is shown, display the first selection screen
		setOnShowing(evt -> {
			wrapper.setCenter(controls);
    		selectionModel.clearSelection();
		});
		
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
    	
    	other.setOnAction(evt -> {
    		LOG.log(Level.INFO, "Opened 'other' option screen...");
    		wrapper.setCenter(fullSelectionList);
    	});

    	Button option3 = makeOptionButton(2);
    	Button option4 = makeOptionButton(3);
    	
    	dummyButton.setVisible(false);
    	dummyButton.getStyleClass().add("catch-select-option");
    	
    	dummyHoverButton.setVisible(false);
    	dummyHoverButton.getStyleClass().add("catch-select-option");
    	dummyHoverButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
    	
    	controls.getChildren().addAll(empty, option2, option3, option4, other, dummyButton, dummyHoverButton);
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
    		catchType = Bindings.valueAt(catchTypes, place-1);
    	}
    	Button button = new Button();
    	button.getStyleClass().add("catch-select-option");
    	
    	addBackgroundLoader(button, catchType);
    	
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
    
    private void addBackgroundLoader (Button button, ObjectBinding<CatchType> catchType) {    	
    	ObjectProperty<Background> bgProperty = new SimpleObjectProperty<>();
    	button.backgroundProperty().bind(bgProperty);
    	
    	BooleanProperty useHoverBackground = new SimpleBooleanProperty(false);
    	
    	//Listen for changes to the button's hover state - when hovered over, use the hover background
    	button.getPseudoClassStates().addListener((SetChangeListener<PseudoClass>) change -> {
    		if (change.wasAdded() && change.getElementAdded() == PseudoClass.getPseudoClass("hover")) {
    			useHoverBackground.set(true);
    		} else if (change.wasRemoved() && change.getElementRemoved() == PseudoClass.getPseudoClass("hover")) {
    			useHoverBackground.set(false);
    		}
    	});
    	
    	//Re-construct the background any time something changes (hover state, background css, catch type)
    	InvalidationListener listener = obs -> {
    		if (obs == dummyButton.backgroundProperty() && useHoverBackground.get()) {
    			//If we're currently using the hover background & the regular background changes, ignore the change
    			return;
    		}
    		if (obs == dummyHoverButton.backgroundProperty() && !useHoverBackground.get()) {
    			//If we're currently using the regular background & the hover background changes, ignore the change
    			return;
    		}
    		Background bg = dummyButton.getBackground();
    		if (useHoverBackground.get()) {
    			bg = dummyHoverButton.getBackground();
    		}
    		if (catchType.get() != null && catchType.get().getImageUrl() != null) {
    			button.pseudoClassStateChanged(imageButtonClass, true);
    			List<BackgroundImage> bgImages = new ArrayList<>();
    			bgImages.add(new BackgroundImage(catchType.get().getImage(), BackgroundRepeat.NO_REPEAT,
    					BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT));
    			bg = new Background(bg == null ? null : bg.getFills(), bgImages);    			
    		} else {
    			button.pseudoClassStateChanged(imageButtonClass, false);    			
    		}
			bgProperty.set(bg);
    	};
    	
    	//Watch for changes on the regular & dummy buttons, the catch type, and whether it's currently hovered over
    	dummyButton.backgroundProperty().addListener(listener);
    	dummyHoverButton.backgroundProperty().addListener(listener);
    	catchType.addListener(listener);
    	useHoverBackground.addListener(listener);
    }
    
}
