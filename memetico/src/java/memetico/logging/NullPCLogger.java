package memetico.logging;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import memetico.Population;
import org.marcos.uon.tspaidemo.util.log.NullLogger;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class NullPCLogger extends NullLogger<MemeticoSnapshot> implements IPCLogger {
    @Override
    public IPCLogger.View newView() {
        return NULL_VIEW;
    }

    @Override
    public void loadJson(JsonElement data) {
    }

    public static final IPCLogger.View NULL_VIEW = new IPCLogger.View() {
        @Override
        public long getStartTime() {
            return 0;
        }

        @Override
        public boolean update() {
            return false;
        }

        @Override
        public boolean tryUpdate() {
            return false;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public JsonElement jsonify() throws InterruptedException {
            JsonObject result = new JsonObject();
            result.add("states", new JsonArray());
            result.addProperty("startTime", getStartTime());
            return result;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<MemeticoSnapshot> iterator() {
            return null;
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }

        @Override
        public boolean add(MemeticoSnapshot memeticoSnapshot) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends MemeticoSnapshot> c) {
            return false;
        }

        @Override
        public boolean addAll(int index, Collection<? extends MemeticoSnapshot> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {
        }

        @Override
        public MemeticoSnapshot get(int index) {
            return null;
        }

        @Override
        public MemeticoSnapshot set(int index, MemeticoSnapshot element) {
            return null;
        }

        @Override
        public void add(int index, MemeticoSnapshot element) {

        }

        @Override
        public MemeticoSnapshot remove(int index) {
            return null;
        }

        @Override
        public int indexOf(Object o) {
            return 0;
        }

        @Override
        public int lastIndexOf(Object o) {
            return 0;
        }

        @Override
        public ListIterator<MemeticoSnapshot> listIterator() {
            return null;
        }

        @Override
        public ListIterator<MemeticoSnapshot> listIterator(int index) {
            return null;
        }

        @Override
        public List<MemeticoSnapshot> subList(int fromIndex, int toIndex) {
            return null;
        }
    };

    @Override
    public void log(String instanceName, Population population, int generation, boolean isFinal) {
    }

    @Override
    public void tryLog(String instanceName, Population population, int generation, boolean isFinal) {
    }
}
