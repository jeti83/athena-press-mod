package pro.jeti.athenapress.integration;

public interface HytalePlayerContextResolver<TPlayer> {

    HytalePlayerContext resolve(TPlayer player);
}