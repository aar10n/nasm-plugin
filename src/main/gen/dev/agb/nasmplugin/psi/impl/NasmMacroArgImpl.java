// This is a generated file. Not intended for manual editing.
package dev.agb.nasmplugin.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static dev.agb.nasmplugin.psi.NasmTypes.*;
import dev.agb.nasmplugin.psi.*;

public class NasmMacroArgImpl extends NasmMacroArgMixin implements NasmMacroArg {

  public NasmMacroArgImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull NasmVisitor visitor) {
    visitor.visitMacroArg(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NasmVisitor) accept((NasmVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<NasmContextRef> getContextRefList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, NasmContextRef.class);
  }

  @Override
  @NotNull
  public List<NasmMacroExpansion> getMacroExpansionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, NasmMacroExpansion.class);
  }

  @Override
  @NotNull
  public List<NasmMacroParamRef> getMacroParamRefList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, NasmMacroParamRef.class);
  }

  @Override
  @NotNull
  public List<NasmOperator> getOperatorList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, NasmOperator.class);
  }

  @Override
  @NotNull
  public List<NasmPreprocessorId> getPreprocessorIdList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, NasmPreprocessorId.class);
  }

  @Override
  @NotNull
  public List<NasmSeparator> getSeparatorList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, NasmSeparator.class);
  }

  @Override
  @NotNull
  public List<NasmStringizeOp> getStringizeOpList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, NasmStringizeOp.class);
  }

  @Override
  @Nullable
  public NasmTokenSequence getTokenSequence() {
    return findChildByClass(NasmTokenSequence.class);
  }

}
