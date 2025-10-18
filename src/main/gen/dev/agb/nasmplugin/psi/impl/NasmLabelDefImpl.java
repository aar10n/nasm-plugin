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

public class NasmLabelDefImpl extends NasmNamedElementImpl implements NasmLabelDef {

  public NasmLabelDefImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull NasmVisitor visitor) {
    visitor.visitLabelDef(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NasmVisitor) accept((NasmVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public NasmGlobalLabel getGlobalLabel() {
    return findChildByClass(NasmGlobalLabel.class);
  }

  @Override
  @Nullable
  public NasmLocalLabel getLocalLabel() {
    return findChildByClass(NasmLocalLabel.class);
  }

}
