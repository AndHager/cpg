/*
 * Copyright (c) 2019, Fraunhofer AISEC. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *                    $$$$$$\  $$$$$$$\   $$$$$$\
 *                   $$  __$$\ $$  __$$\ $$  __$$\
 *                   $$ /  \__|$$ |  $$ |$$ /  \__|
 *                   $$ |      $$$$$$$  |$$ |$$$$\
 *                   $$ |      $$  ____/ $$ |\_$$ |
 *                   $$ |  $$\ $$ |      $$ |  $$ |
 *                   \$$$$$   |$$ |      \$$$$$   |
 *                    \______/ \__|       \______/
 *
 */

package de.fraunhofer.aisec.cpg.graph;

import de.fraunhofer.aisec.cpg.graph.HasType.TypeListener;
import de.fraunhofer.aisec.cpg.graph.Type.Origin;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * An expression, which calls another function. It has a list of arguments (list of {@link
 * Expression}s) and is connected via the INVOKES edge to its {@link FunctionDeclaration}.
 */
public class CallExpression extends Expression implements TypeListener {

  /**
   * Connection to its {@link FunctionDeclaration}. This will be populated by the {@link
   * de.fraunhofer.aisec.cpg.passes.CallResolver}.
   */
  protected List<FunctionDeclaration> invokes = new ArrayList<>();
  /** The list of arguments. */
  @SubGraph("AST")
  private List<Expression> arguments = new ArrayList<>();
  /**
   * The base object. This is marked as an AST child, because this is required for {@link
   * MemberCallExpression}. Be aware that for simple calls the implicit "this" base is not part of
   * the original AST, but we treat it as such for better consistency
   */
  @SubGraph("AST")
  private Node base;

  private String fqn;

  public Node getBase() {
    return base;
  }

  public void setBase(Node base) {
    this.base = base;
  }

  public List<Expression> getArguments() {
    return arguments;
  }

  public void setArguments(List<Expression> arguments) {
    this.arguments = arguments;
  }

  public List<FunctionDeclaration> getInvokes() {
    return invokes;
  }

  public void setInvokes(List<FunctionDeclaration> invokes) {
    this.invokes.forEach(
        i -> {
          i.unregisterTypeListener(this);
          for (ParamVariableDeclaration param : i.getParameters()) {
            // A param could be variadic, so multiple arguments could be set as incoming DFG
            param.getPrevDFG().stream()
                .filter(x -> arguments.contains(x))
                .forEach(param::removeNextDFG);
          }
          this.removePrevDFG(i);
        });
    this.invokes = invokes;
    invokes.forEach(
        i -> {
          i.registerTypeListener(this);
          i.getParameters().sort(Comparator.comparing(ParamVariableDeclaration::getArgumentIndex));
          for (int j = 0; j < arguments.size(); j++) {
            ParamVariableDeclaration param = i.getParameters().get(j);
            if (param.isVariadic()) {
              for (; j < arguments.size(); j++) {
                // map all the following arguments to this variadic param
                param.addPrevDFG(arguments.get(j));
              }
              break;
            } else {
              param.addPrevDFG(arguments.get(j));
            }
          }
          this.addPrevDFG(i);
        });
  }

  public List<Type> getSignature() {
    return getArguments().stream().map(Expression::getType).collect(Collectors.toList());
  }

  @Override
  public void typeChanged(HasType src, HasType root, Type oldType) {
    Type previous = this.type;

    List<Type> types =
        invokes.stream()
            .map(FunctionDeclaration::getType)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    Type alternative = !types.isEmpty() ? types.get(0) : null;
    Type commonType = TypeManager.getInstance().getCommonType(types).orElse(alternative);
    Set<Type> subTypes = new HashSet<>(getPossibleSubTypes());
    subTypes.remove(oldType);
    subTypes.addAll(types);

    setType(commonType, root);
    setPossibleSubTypes(subTypes, root);

    if (!previous.equals(this.type)) {
      this.type.setTypeOrigin(Origin.DATAFLOW);
    }
  }

  @Override
  public void possibleSubTypesChanged(HasType src, HasType root, Set<Type> oldSubTypes) {
    Set<Type> subTypes = new HashSet<>(getPossibleSubTypes());
    subTypes.addAll(src.getPossibleSubTypes());
    setPossibleSubTypes(subTypes, root);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, Node.TO_STRING_STYLE)
        .appendSuper(super.toString())
        .append("arguments", arguments)
        .append("invokes", invokes)
        .append("base", base)
        .toString();
  }

  public String getFqn() {
    return fqn;
  }

  public void setFqn(String fqn) {
    this.fqn = fqn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CallExpression)) {
      return false;
    }
    CallExpression that = (CallExpression) o;
    return super.equals(that)
        && Objects.equals(arguments, that.arguments)
        && Objects.equals(invokes, that.invokes)
        && Objects.equals(base, that.base);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
