package org.nestnz.app.views;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.NestApplication;
import org.nestnz.app.model.Region;
import org.nestnz.app.model.Trapline;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.CharmListCell;
import com.gluonhq.charm.glisten.control.CharmListView;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.util.StringConverter;

public class TraplineListView extends View {
	
	public static final String NAME = MobileApplication.HOME_VIEW;//"trapline_list";

    private static final Logger LOG = Logger.getLogger(TraplineListView.class.getName());
	
	private final CharmListView<Trapline, Region> traplineList;

    public TraplineListView(ObservableList<Trapline> traplines) {
        super(NAME);
        traplineList = new CharmListView<>(traplines);
        traplineList.setHeadersFunction(Trapline::getRegion);
        traplineList.setConverter(new StringConverter <Region>() {
            @Override public String toString(Region r) {
                return r.getName();
            }
			@Override public Region fromString(String string) {
				throw new UnsupportedOperationException("Not supported!");
			}
        });
        
        traplineList.setCellFactory(list -> new CharmListCell<Trapline>() {
        	Button button = new Button();
        	Trapline trapline;
        	
        	{
        		button.setMaxHeight(1000.0);
        		button.setMaxWidth(1000.0);
        		this.setGraphic(button);
        		button.setOnAction(evt -> {
        			LOG.log(Level.INFO, "Clicked trapline: "+trapline);
        			TraplineInfoView infoView = ((NestApplication) TraplineListView.this.getApplication()).lookupView(TraplineInfoView.NAME);
        			infoView.setTrapline(trapline);
        			TraplineListView.this.getApplication().switchView(TraplineInfoView.NAME);
        		});
        	}
        	
        	@Override public void updateItem (Trapline item, boolean empty) {
        		super.updateItem(item, empty);
        		trapline = item;
        		if(item!=null && !empty){
        			button.setText(item.getName());
                } else {
                	button.setText(null);
                }
        	}
        });
        
        setCenter(traplineList);
        getStylesheets().add(TraplineListView.class.getResource("traplineView.css").toExternalForm());
    }

    @Override
    protected void updateAppBar(AppBar appBar) {
        appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> MobileApplication.getInstance().showLayer(NestApplication.MENU_LAYER)));
        appBar.setTitleText("Nest NZ");
        //appBar.getActionItems().add(MaterialDesignIcon.SEARCH.button(e -> System.out.println("Search")));
    }
    
}