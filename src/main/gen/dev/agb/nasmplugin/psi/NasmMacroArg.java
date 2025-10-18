// This is a generated file. Not intended for manual editing.
package dev.agb.nasmplugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NasmMacroArg extends PsiElement {

  @NotNull
  List<NasmContextRef> getContextRefList();

  @NotNull
  List<NasmMacroExpansion> getMacroExpansionList();

  @NotNull
  List<NasmMacroParamRef> getMacroParamRefList();

  @NotNull
  List<NasmOperator> getOperatorList();

  @NotNull
  List<NasmPreprocessorId> getPreprocessorIdList();

  @NotNull
  List<NasmSeparator> getSeparatorList();

  @NotNull
  List<NasmStringizeOp> getStringizeOpList();

  @Nullable
  NasmTokenSequence getTokenSequence();

}
