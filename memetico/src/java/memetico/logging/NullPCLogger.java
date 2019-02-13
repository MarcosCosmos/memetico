package memetico.logging;

import memetico.Population;
import org.marcos.uon.tspaidemo.util.log.NullLogger;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class NullPCLogger extends NullLogger<PCAlgorithmState> implements IPCLogger {
    @Override
    public IPCLogger.View newView() {
        return new IPCLogger.View(){
            @Override
            public double getStartTime() {
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
            public Iterator<PCAlgorithmState> iterator() {
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
            public boolean add(PCAlgorithmState pcAlgorithmState) {
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
            public boolean addAll(Collection<? extends PCAlgorithmState> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends PCAlgorithmState> c) {
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
            public PCAlgorithmState get(int index) {
                return null;
            }

            @Override
            public PCAlgorithmState set(int index, PCAlgorithmState element) {
                return null;
            }

            @Override
            public void add(int index, PCAlgorithmState element) {

            }

            @Override
            public PCAlgorithmState remove(int index) {
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
            public ListIterator<PCAlgorithmState> listIterator() {
                return null;
            }

            @Override
            public ListIterator<PCAlgorithmState> listIterator(int index) {
                return null;
            }

            @Override
            public List<PCAlgorithmState> subList(int fromIndex, int toIndex) {
                return null;
            }
        };
    }

    @Override
    public void log(String instanceName, Population population, int generation) {
    }

    @Override
    public void tryLog(String instanceName, Population population, int generation) {
    }
}
