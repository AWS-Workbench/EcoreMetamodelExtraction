package eme.properties;

/**
 * Interface for binary properties used with an instance of {@link AbstractProperties}.
 * @author Timur Saglam
 */
public interface IBinaryProperty {
    /**
     * Accessor for the default value.
     * @return the default value.
     */
    boolean getDefaultValue();

    /**
     * Accessor for the key String.
     * @return the key.
     */
    String getKey();
}