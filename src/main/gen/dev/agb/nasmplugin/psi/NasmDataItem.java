// This is a generated file. Not intended for manual editing.
package dev.agb.nasmplugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NasmDataItem extends PsiElement {

  @Nullable
  NasmConditionalData getConditionalData();

  @Nullable
  NasmDataList getDataList();

  @Nullable
  NasmExpression getExpression();

  @Nullable
  NasmMacroExpansion getMacroExpansion();

  @Nullable
  NasmStringFunction getStringFunction();

}
