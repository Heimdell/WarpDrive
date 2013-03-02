package com.maelstrom.astronomicon.stubs;

import com.maelstrom.astronomicon.workers.IGenerates;


public class DummyWorker<T> implements IGenerates<T> {

    int call_times = 3;
    
    T prepared;
    
    public DummyWorker(T prepared) {
        this.prepared = prepared;
    }
    
    @Override
    public void process(int max_count) {
        assert hasWork();
        
        call_times--;
    }

    @Override
    public boolean hasWork() {
        // TODO Auto-generated method stub
        return call_times > 0;
    }

    @Override
    public T results() {
        // TODO Auto-generated method stub
        return prepared;
    }

}
