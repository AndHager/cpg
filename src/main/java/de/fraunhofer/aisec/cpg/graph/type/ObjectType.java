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

package de.fraunhofer.aisec.cpg.graph.type;

import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.cpg.graph.RecordDeclaration;
import de.fraunhofer.aisec.cpg.graph.edge.PropertyEdge;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * This is the main type in the Type system. ObjectTypes describe objects, as instances of a class.
 * This also includes primitive data types.
 */
public class ObjectType extends Type {

  /**
   * ObjectTypes can have a modifier if they are primitive datatypes. The default is signed for
   * primitive data types if there is no more information provided. In case of non primitive
   * datatypes the modifier is NOT_APPLICABLE
   */
  public enum Modifier {
    SIGNED,
    UNSIGNED,
    NOT_APPLICABLE,
  }

  private final Modifier modifier;
  private final boolean primitive;
  // Reference from the ObjectType to its class (RecordDeclaration) only if the class is available
  private RecordDeclaration recordDeclaration = null;

  @Relationship(type = "GENERICS", direction = Relationship.OUTGOING)
  private List<GenericEdge> generics;

  @RelationshipEntity(type = "GENERICS")
  public static class GenericEdge extends PropertyEdge {
    @Property private int index;

    public GenericEdge(Node start, Node target, int index) {
      super(start, target);
      this.index = index;
    }

    public int getIndex() {
      return index;
    }
  }

  public ObjectType(
      String typeName,
      Storage storage,
      Qualifier qualifier,
      List<Type> generics,
      Modifier modifier,
      boolean primitive) {
    super(typeName, storage, qualifier);
    this.generics = convertToGenericEdge(generics);
    this.modifier = modifier;
    this.primitive = primitive;
  }

  public ObjectType(Type type, List<Type> generics, Modifier modifier, boolean primitive) {
    super(type);
    this.generics = convertToGenericEdge(generics);
    this.modifier = modifier;
    this.primitive = primitive;
  }

  public ObjectType() {
    super();
    this.generics = new ArrayList<>();
    this.modifier = Modifier.NOT_APPLICABLE;
    this.primitive = false;
  }

  private List<GenericEdge> convertToGenericEdge(List<Type> generics) {
    List<GenericEdge> genericEdges = new ArrayList<>();
    int counter = 0;
    for (Type t : generics) {
      genericEdges.add(new GenericEdge(this, t, counter));
      counter++;
    }

    return genericEdges;
  }

  /**
   * @deprecated
   */
  @Deprecated
  public List<Type> getGenerics() {
    List<Type> genericValues = new ArrayList<>();
    for (GenericEdge edge : this.generics) {
      genericValues.add((Type) edge.getEnd());
    }
    return genericValues;
  }

  public List<GenericEdge> getGenericEdges() {
    return this.generics;
  }

  public RecordDeclaration getRecordDeclaration() {
    return recordDeclaration;
  }

  public void setRecordDeclaration(RecordDeclaration recordDeclaration) {
    this.recordDeclaration = recordDeclaration;
  }

  /** @return PointerType to a ObjectType, e.g. int* */
  @Override
  public PointerType reference(PointerType.PointerOrigin pointerOrigin) {
    return new PointerType(this, pointerOrigin);
  }

  /**
   * @return UnknownType, as we cannot infer any type information when dereferencing an ObjectType,
   *     as it is just some memory and its interpretation is unknown
   */
  @Override
  public Type dereference() {
    return UnknownType.getUnknownType();
  }

  @Override
  public Type duplicate() {
    return new ObjectType(this, this.getGenerics(), this.modifier, this.primitive);
  }

  public void setGenerics(List<Type> generics) {
    this.generics = convertToGenericEdge(generics);
  }

  @Override
  public boolean isSimilar(Type t) {
    return t instanceof ObjectType
        && this.getGenerics().equals(((ObjectType) t).getGenerics())
        && super.isSimilar(t);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ObjectType)) return false;
    if (!super.equals(o)) return false;
    ObjectType that = (ObjectType) o;
    return Objects.equals(generics, that.generics)
        && this.primitive == that.primitive
        && this.modifier.equals(that.modifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), generics, modifier, primitive);
  }

  @Override
  public String toString() {
    return "ObjectType{"
        + "generics="
        + generics
        + ", typeName='"
        + name
        + '\''
        + ", storage="
        + this.getStorage()
        + ", qualifier="
        + this.getQualifier()
        + ", modifier="
        + modifier
        + ", primitive="
        + primitive
        + ", origin="
        + this.getTypeOrigin()
        + '}';
  }
}
