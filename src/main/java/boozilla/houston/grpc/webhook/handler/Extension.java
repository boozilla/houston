package boozilla.houston.grpc.webhook.handler;

import boozilla.houston.grpc.webhook.GitBehavior;

import java.lang.reflect.InvocationTargetException;

public enum Extension {
    XLSX_ASSET_WORKBOOK(XlsxWorkbookHandler.class, "xlsx", ".xml"),
    JSON_MANIFEST(JsonManifestHandler.class, "json");

    private final Class<? extends GitFileHandler> handlerClass;
    private final String[] extensions;

    Extension(final Class<? extends GitFileHandler> handlerClass, final String... extensions)
    {
        this.handlerClass = handlerClass;
        this.extensions = extensions;
    }

    public static Extension of(final String filename)
    {
        final var split = filename.split("/");
        final var name = split[split.length - 1];

        if(name.startsWith("~$"))
            return null;

        for(final var extension : values())
        {
            for(final var ext : extension.extensions)
            {
                if(name.endsWith(ext))
                {
                    return extension;
                }
            }
        }

        return null;
    }

    public Class<? extends GitFileHandler> handlerClass()
    {
        return handlerClass;
    }

    public String[] getExtensions()
    {
        return extensions;
    }

    public GitFileHandler handler(final String projectId, final String issueId, final String commitId, final String packageName,
                                  final GitBehavior<?> behavior)
    {
        try
        {
            //noinspection JavaReflectionInvocation
            return handlerClass.getDeclaredConstructor(GitFileHandler.class.getConstructors()[0].getParameterTypes())
                    .newInstance(projectId, issueId, commitId, packageName, behavior);
        }
        catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
        {
            throw new RuntimeException("Unable to create a Git file handler", e);
        }
    }
}
