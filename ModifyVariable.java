package org.spongepowered.asm.mixin.injection;
import java.lang.annotation.*;
@Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD)
public @interface ModifyVariable{ String[] method(); At at(); int ordinal() default -1; boolean argsOnly() default false; int require() default -1;}
