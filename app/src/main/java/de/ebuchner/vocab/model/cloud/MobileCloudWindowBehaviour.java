package de.ebuchner.vocab.model.cloud;

public abstract class MobileCloudWindowBehaviour extends DefaultCloudWindowBehaviour {
    public abstract void refreshBegin();

    public abstract void refreshFinished();

    public abstract void downloadBegin();

    public abstract void downloadFinished();

    public abstract String getServerUrl();

    public abstract void exceptionOccurred(Exception exception);
}
