package memetico;

public class LinkedList {
    public final class Element {

        int datum = -1;
        Element next = null;

        public int getDatum() {
            return datum;
        }

        public Element getNext() {
            return next;
        }

        public void insertAfter(int dado) {
            next = new Element(dado, next);
            if (tail == this)
                tail = next;
        }

        public void insertBefore(int dado) {
            Element element = new Element(dado, this);
            if (this == head) {
                head = element;
                return;
            }
            Element element1;
            for (element1 = head; element1 != null && element1.next != this; element1 = element1.next) ;
            element1.next = element;
        }

        public void extract() {
            Element element = null;
            if (head == this) {
                head = next;
            } else {
                for (element = head; element != null && element.next != this; element = element.next) ;
                if (element == null)
                    throw new RuntimeException();
                element.next = next;
            }
            if (tail == this)
                tail = element;
        }

        Element(int dado, Element element) {
            datum = dado;
            next = element;
        }
    }


    protected Element head = null;
    protected Element tail = null;

    public LinkedList() {
    }

    public void purge() {
        head = null;
        tail = null;
    }

    public Element getHead() {
        return head;
    }

    public Element getTail() {
        return tail;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public int getFirst() {
        if (head == null)
            throw new RuntimeException();
        else
            return head.datum;
    }

    public int getLast() {
        if (tail == null)
            throw new RuntimeException("Empty List");
        else
            return tail.datum;
    }

    public void prepend(int dado) {
        Element element = new Element(dado, head);
        if (head == null)
            tail = element;
        head = element;
    }

    public void append(int dado) {
        Element element = new Element(dado, null);
        if (head == null)
            head = element;
        else
            tail.next = element;
        tail = element;
    }

    public void assign(LinkedList linkedlist) {
        if (linkedlist != this) {
            purge();
            for (Element element = linkedlist.head; element != null; element = element.next)
                append(element.datum);

        }
    }

    public void extract(int dado) {
        Element element = head;
        Element element1 = null;
        for (; element != null && element.datum != dado; element = element.next)
            element1 = element;

        if (element == null)
            throw new RuntimeException("item not found");
        if (element == head)
            head = element.next;
        else
            element1.next = element.next;
        if (element == tail)
            tail = element1;
    }
}
