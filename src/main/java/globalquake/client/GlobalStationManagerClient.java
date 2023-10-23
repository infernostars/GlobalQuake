package globalquake.client;

import globalquake.core.station.AbstractStation;
import globalquake.core.station.GlobalStationManager;
import globalquake.database.StationDatabaseManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GlobalStationManagerClient extends GlobalStationManager {

    private List<AbstractStation> stations;

    public GlobalStationManagerClient(){
        stations = new CopyOnWriteArrayList<>();
    }

    @Override
    public void initStations(StationDatabaseManager databaseManager) {

    }

    @Override
    public List<AbstractStation> getStations() {
        return stations;
    }
}
