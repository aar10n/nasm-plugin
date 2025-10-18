// This is a generated file. Not intended for manual editing.
package dev.agb.nasmplugin.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static dev.agb.nasmplugin.psi.NasmTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import dev.agb.nasmplugin.psi.*;

public class NasmBracketedDirectiveImpl extends ASTWrapperPsiElement implements NasmBracketedDirective {

  public NasmBracketedDirectiveImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull NasmVisitor visitor) {
    visitor.visitBracketedDirective(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NasmVisitor) accept((NasmVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public NasmDefaultOption getDefaultOption() {
    return findChildByClass(NasmDefaultOption.class);
  }

}
