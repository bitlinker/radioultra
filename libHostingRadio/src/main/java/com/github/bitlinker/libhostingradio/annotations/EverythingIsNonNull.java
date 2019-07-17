package com.github.bitlinker.libhostingradio.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;

/**
 * Makes everything non-null within the given package.
 *
 * Example package-info.java:
 *
 * @EverythingIsNonNull
 * package com.github.bitlinker.streamutils;
 *
 * import com.github.bitlinker.streamutils.annotations.EverythingIsNonNull;
 */
@Nonnull
@TypeQualifierDefault({
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
public @interface EverythingIsNonNull {
}