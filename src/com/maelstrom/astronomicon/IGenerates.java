package com.maelstrom.astronomicon;

public interface IGenerates<Result> {

    public abstract void process(int max_count);

    public abstract boolean hasWork();
    
    Result results()
        throws ForbiddenSourceBlockException,
               ForbiddenDestinationBlockException;

}
