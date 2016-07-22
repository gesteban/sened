package es.unizar.sened.model;

public class VoxResource {

  public String uri;
  public String label;
  public String abstractText;
  public String relation;

  public VoxResource(String uri) {
    this.uri = uri;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((uri == null) ? 0 : uri.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    VoxResource other = (VoxResource) obj;
    if (uri == null) {
      if (other.uri != null)
        return false;
    } else if (!uri.equals(other.uri))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "VoxResource [uri=" + uri + ", label=" + label + ", abstractText=" + abstractText.length() + ", relation="
        + relation + "]";
  }

  public String get(String key) {
    switch (key) {
    case "label":
      return label;
    case "abstractText":
      return abstractText;
    case "uri":
      return uri;
    case "relation":
      return relation;
    }
    return "<not_key_found>";
  }

}
