// This is a generated file. Not intended for manual editing.
package dev.agb.nasmplugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NasmOperand extends PsiElement {

  @NotNull
  List<NasmDecorator> getDecoratorList();

  @Nullable
  NasmExpandedOperand getExpandedOperand();

}
