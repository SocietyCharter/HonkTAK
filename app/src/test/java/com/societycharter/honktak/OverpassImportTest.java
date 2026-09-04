package com.societycharter.honktak;

import static org.junit.Assert.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;

public class OverpassImportTest {
    private static byte[] bytes(String s){ return s.getBytes(StandardCharsets.UTF_8); }
    @Test public void queryIsViewportBoundedAndReadOnly(){ String q=OverpassImport.query(35.9,-96.0,36.2,-95.7); assertTrue(q.contains("(35.9,-96.0,36.2,-95.7)")); assertTrue(q.contains("surveillance:type")); assertFalse(q.toLowerCase().contains("update")); }
    @Test(expected=IllegalArgumentException.class) public void oversizedViewportRejected(){ OverpassImport.query(34,-96,36,-95); }
    @Test public void parserIsStrictBoundedAndDedupes(){ String node="{\"type\":\"node\",\"id\":7,\"lat\":36.1,\"lon\":-95.8,\"tags\":{\"man_made\":\"surveillance\",\"surveillance:type\":\"ALPR\",\"camera:direction\":\"90\",\"operator\":\"city\"}}"; List<OverpassImport.Camera> out=OverpassImport.parse(bytes("{\"elements\":["+node+","+node+",{\"type\":\"way\",\"id\":8}]}")); assertEquals(1,out.size()); assertEquals("osm:node:7",out.get(0).sourceId); assertEquals(Integer.valueOf(90),out.get(0).direction); }
    @Test(expected=IllegalArgumentException.class) public void responseByteLimitEnforced(){ OverpassImport.parse(new byte[OverpassImport.MAX_RESPONSE_BYTES+1]); }
    @Test public void outOfRangeCoordinatesAreRejected(){ assertTrue(OverpassImport.parse(bytes("{\"elements\":[{\"type\":\"node\",\"id\":1,\"lat\":999,\"lon\":999,\"tags\":{\"man_made\":\"surveillance\",\"surveillance:type\":\"ALPR\"}}]}" )).isEmpty()); }
    @Test public void defeatedOverrideSurvivesRefreshAndUndoRestoresActive(){ OverpassImport.Camera c=OverpassImport.parse(bytes("{\"elements\":[{\"type\":\"node\",\"id\":9,\"lat\":36,\"lon\":-95,\"tags\":{\"man_made\":\"surveillance\",\"surveillance:type\":\"ALPR\",\"direction\":\"45\"}}]}" )).get(0); ImportedCameraState s=new ImportedCameraState(); s.restoreOverride(c.sourceId,ImportedCameraState.encodeOverride(true,1)); assertTrue(s.merge(Arrays.asList(c)).containsKey(c.sourceId)); assertTrue(s.isRemoved(c.sourceId)); assertFalse(ImportedCameraState.showWedge(c,true)); assertEquals("goose_red_x",ImportedCameraState.iconVariant(true)); assertEquals(ImportedCameraState.ACTION_MARK_ACTIVE,s.availableAction(c.sourceId)); s.restoreOverride(c.sourceId,ImportedCameraState.encodeOverride(false,2)); assertFalse(s.isRemoved(c.sourceId)); assertEquals(ImportedCameraState.ACTION_MARK_DEFEATED,s.availableAction(c.sourceId)); }
    @Test public void noSelectionHasNoAction(){ assertEquals(ImportedCameraState.ACTION_NONE,new ImportedCameraState().availableAction(null)); }
    @Test public void importFirstPathInitializesDimensionsBeforeUiCanLoad() throws Exception {
        String receiver=source("HonkTakDropDownReceiver.java");
        int prepareCall=receiver.indexOf("prepareGooseImage();"), bindCall=receiver.indexOf("bindUi();");
        assertTrue(prepareCall>=0&&bindCall>prepareCall);
        String prepare=slice(receiver,"private void prepareGooseImage()","private Icon gooseIcon");
        assertTrue(prepare.contains("gooseRenderedWidth = GooseIconScale.dimension"));
        assertTrue(prepare.contains("gooseRenderedHeight = GooseIconScale.dimension"));
        assertTrue(GooseIconScale.dimension(64,1.0,1.0)>0);
    }
    @Test public void importAndMarkPathsHaveNoShareOrExternalWriteCapability() throws Exception {
        String receiver=source("HonkTakDropDownReceiver.java");
        String load=slice(receiver,"private void loadCamerasInView()","private void renderImported");
        String mark=slice(receiver,"private void setSelectedImportedRemoved","private void updateImportActionUi");
        for(String code:new String[]{load,mark}) { assertFalse(code.contains("dispatchToBroadcast")); assertFalse(code.contains("shareGate")); assertFalse(code.contains("HonkCotCodec")); assertFalse(code.contains("send")); }
        assertTrue(load.contains("OverpassClient.load(query)"));
        assertFalse(mark.contains("OverpassClient")); assertFalse(mark.contains("Http"));
        String client=source("OverpassClient.java");
        assertTrue(client.contains("https://overpass.deflock.org/api/interpreter"));
        assertTrue(client.contains("https://overpass-api.de/api/interpreter"));
        String endpoints=slice(client,"static final String[] ENDPOINTS","private OverpassClient()");
        assertEquals(2, endpoints.split("https://",-1).length-1);
        assertTrue(client.contains("setRequestMethod(\"POST\")"));
        assertTrue(client.contains("application/x-www-form-urlencoded"));
        assertFalse(client.contains("setRequestMethod(\"PUT\")")); assertFalse(client.contains("setRequestMethod(\"DELETE\")")); assertFalse(client.contains("setRequestMethod(\"PATCH\")"));
        assertTrue(client.contains("User-Agent")); assertTrue(client.contains("https://github.com/SocietyCharter/HonkTAK"));
    }
    @Test public void importedGooseAndWedgeSelectionRevealDefeatedAction() throws Exception {
        String receiver=source("HonkTakDropDownReceiver.java");
        String mapEvent=slice(receiver,"@Override public void onMapEvent","private void updateAim");
        assertTrue(mapEvent.contains("MapEvent.ITEM_CLICK"));
        assertTrue(mapEvent.contains("MapEvent.ITEM_LONG_PRESS"));
        assertTrue(mapEvent.contains("selectImportedSource(source)"));
        String render=slice(receiver,"private void renderImported","private Icon importIcon");
        assertTrue(render.contains("marker.setMetaString(\"honktak.source_id\",c.sourceId)"));
        assertTrue(render.contains("wedge.setMetaString(\"honktak.source_id\",c.sourceId)"));
        assertTrue(render.contains("setMetaBoolean(\"clickable\",true)"));
        String select=slice(receiver,"private void selectImportedSource","private void setSelectedImportedRemoved");
        assertTrue(select.contains("updateImportActionUi()"));
        assertTrue(select.contains("showDropDown"));
        assertTrue(select.contains("unhideDropDown"));
        String update=slice(receiver,"private void updateImportActionUi","private static final class ImportedRecord");
        assertTrue(update.contains("ACTION_MARK_DEFEATED"));
        assertTrue(update.contains("R.id.mark_taken_down"));
    }
    @Test public void defeatedStyleIsDesaturatedRedXAndUsesSmallerDimensions() throws Exception {
        String receiver=source("HonkTakDropDownReceiver.java");
        String prepare=slice(receiver,"private void prepareGooseImage()","private Icon gooseIcon");
        assertTrue(prepare.contains("setSaturation(0.22f)"));
        assertTrue(prepare.contains("defeatedCanvas.drawLine"));
        String icon=slice(receiver,"private Icon importIcon","private void restoreImportOverrides");
        assertTrue(icon.contains("GooseIconScale.importedDimension"));
        assertTrue(icon.contains("removed?redXImageUri:gooseImageUri"));
    }
    @Test public void everyGooseRenderPathUsesTheGlobalHalfSizedDimensions() throws Exception {
        String receiver=source("HonkTakDropDownReceiver.java");
        String addLocal=slice(receiver,"private void addLocal","@Override public void onCotEvent");
        assertTrue(addLocal.contains("marker.setIcon(gooseIcon(getMapView().getMapScale()))"));
        String inbound=slice(receiver,"@Override public void onCotEvent","private List<HonkPolicy.Sighting>");
        assertTrue(inbound.contains("handler.post(() -> addLocal(observation, true))"));
        String restored=slice(receiver,"private void restorePersisted","private String selected");
        assertTrue(restored.contains("addLocal(observation, false)"));
        String gooseIcon=slice(receiver,"private Icon gooseIcon","@Override public void onMapMoved");
        assertTrue(gooseIcon.contains("GooseIconScale.dimension(gooseBaseWidth"));
        assertTrue(gooseIcon.contains("GooseIconScale.dimension(gooseBaseHeight"));
        assertTrue(gooseIcon.contains("setSize(gooseRenderedWidth, gooseRenderedHeight)"));
        String mapMove=slice(receiver,"@Override public void onMapMoved","private void beginPlacement");
        assertTrue(mapMove.contains("GooseIconScale.dimension(gooseBaseWidth"));
        assertTrue(mapMove.contains("for (Record record : sightings) record.marker.setIcon(icon)"));
        assertTrue(mapMove.contains("record.marker.setIcon(importIcon"));
        String imported=slice(receiver,"private Icon importIcon","private void restoreImportOverrides");
        assertTrue(imported.contains("GooseIconScale.importedDimension(gooseRenderedWidth"));
        assertTrue(imported.contains("GooseIconScale.importedDimension(gooseRenderedHeight"));
        assertTrue(imported.contains("setSize(width,height)"));
    }
    private static String source(String name) throws Exception {
        Path path=Path.of("src/main/java/com/societycharter/honktak/"+name);
        if(!Files.isRegularFile(path)) path=Path.of("app").resolve(path);
        return new String(Files.readAllBytes(path),StandardCharsets.UTF_8);
    }
    private static String slice(String text,String start,String end){ int a=text.indexOf(start),b=text.indexOf(end,a); assertTrue(a>=0&&b>a); return text.substring(a,b); }
}
