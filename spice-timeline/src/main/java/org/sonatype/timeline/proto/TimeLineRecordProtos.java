// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: resources/proto/timelinerecord.proto

package org.sonatype.timeline.proto;

public final class TimeLineRecordProtos {
  private TimeLineRecordProtos() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public static final class TimeLineRecord extends
      com.google.protobuf.GeneratedMessage {
    // Use TimeLineRecord.newBuilder() to construct.
    private TimeLineRecord() {
      initFields();
    }
    private TimeLineRecord(boolean noInit) {}
    
    private static final TimeLineRecord defaultInstance;
    public static TimeLineRecord getDefaultInstance() {
      return defaultInstance;
    }
    
    public TimeLineRecord getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.sonatype.timeline.proto.TimeLineRecordProtos.internal_static_timeline_TimeLineRecord_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.sonatype.timeline.proto.TimeLineRecordProtos.internal_static_timeline_TimeLineRecord_fieldAccessorTable;
    }
    
    public static final class Data extends
        com.google.protobuf.GeneratedMessage {
      // Use Data.newBuilder() to construct.
      private Data() {
        initFields();
      }
      private Data(boolean noInit) {}
      
      private static final Data defaultInstance;
      public static Data getDefaultInstance() {
        return defaultInstance;
      }
      
      public Data getDefaultInstanceForType() {
        return defaultInstance;
      }
      
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return org.sonatype.timeline.proto.TimeLineRecordProtos.internal_static_timeline_TimeLineRecord_Data_descriptor;
      }
      
      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return org.sonatype.timeline.proto.TimeLineRecordProtos.internal_static_timeline_TimeLineRecord_Data_fieldAccessorTable;
      }
      
      // required string key = 1;
      public static final int KEY_FIELD_NUMBER = 1;
      private boolean hasKey;
      private java.lang.String key_ = "";
      public boolean hasKey() { return hasKey; }
      public java.lang.String getKey() { return key_; }
      
      // optional string value = 2;
      public static final int VALUE_FIELD_NUMBER = 2;
      private boolean hasValue;
      private java.lang.String value_ = "";
      public boolean hasValue() { return hasValue; }
      public java.lang.String getValue() { return value_; }
      
      private void initFields() {
      }
      public final boolean isInitialized() {
        if (!hasKey) return false;
        return true;
      }
      
      public void writeTo(com.google.protobuf.CodedOutputStream output)
                          throws java.io.IOException {
        getSerializedSize();
        if (hasKey()) {
          output.writeString(1, getKey());
        }
        if (hasValue()) {
          output.writeString(2, getValue());
        }
        getUnknownFields().writeTo(output);
      }
      
      private int memoizedSerializedSize = -1;
      public int getSerializedSize() {
        int size = memoizedSerializedSize;
        if (size != -1) return size;
      
        size = 0;
        if (hasKey()) {
          size += com.google.protobuf.CodedOutputStream
            .computeStringSize(1, getKey());
        }
        if (hasValue()) {
          size += com.google.protobuf.CodedOutputStream
            .computeStringSize(2, getValue());
        }
        size += getUnknownFields().getSerializedSize();
        memoizedSerializedSize = size;
        return size;
      }
      
      public static org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data parseFrom(
          com.google.protobuf.ByteString data)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return newBuilder().mergeFrom(data).buildParsed();
      }
      public static org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data parseFrom(
          com.google.protobuf.ByteString data,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return newBuilder().mergeFrom(data, extensionRegistry)
                 .buildParsed();
      }
      public static org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data parseFrom(byte[] data)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return newBuilder().mergeFrom(data).buildParsed();
      }
      public static org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data parseFrom(
          byte[] data,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return newBuilder().mergeFrom(data, extensionRegistry)
                 .buildParsed();
      }
      public static org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data parseFrom(java.io.InputStream input)
          throws java.io.IOException {
        return newBuilder().mergeFrom(input).buildParsed();
      }
      public static org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data parseFrom(
          java.io.InputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        return newBuilder().mergeFrom(input, extensionRegistry)
                 .buildParsed();
      }
      public static org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data parseDelimitedFrom(java.io.InputStream input)
          throws java.io.IOException {
        Builder builder = newBuilder();
        if (builder.mergeDelimitedFrom(input)) {
          return builder.buildParsed();
        } else {
          return null;
        }
      }
      public static org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data parseDelimitedFrom(
          java.io.InputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        Builder builder = newBuilder();
        if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
          return builder.buildParsed();
        } else {
          return null;
        }
      }
      public static org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data parseFrom(
          com.google.protobuf.CodedInputStream input)
          throws java.io.IOException {
        return newBuilder().mergeFrom(input).buildParsed();
      }
      public static org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data parseFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        return newBuilder().mergeFrom(input, extensionRegistry)
                 .buildParsed();
      }
      
      public static Builder newBuilder() { return Builder.create(); }
      public Builder newBuilderForType() { return newBuilder(); }
      public static Builder newBuilder(org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data prototype) {
        return newBuilder().mergeFrom(prototype);
      }
      public Builder toBuilder() { return newBuilder(this); }
      
      public static final class Builder extends
          com.google.protobuf.GeneratedMessage.Builder<Builder> {
        private org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data result;
        
        // Construct using org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data.newBuilder()
        private Builder() {}
        
        private static Builder create() {
          Builder builder = new Builder();
          builder.result = new org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data();
          return builder;
        }
        
        protected org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data internalGetResult() {
          return result;
        }
        
        public Builder clear() {
          if (result == null) {
            throw new IllegalStateException(
              "Cannot call clear() after build().");
          }
          result = new org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data();
          return this;
        }
        
        public Builder clone() {
          return create().mergeFrom(result);
        }
        
        public com.google.protobuf.Descriptors.Descriptor
            getDescriptorForType() {
          return org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data.getDescriptor();
        }
        
        public org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data getDefaultInstanceForType() {
          return org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data.getDefaultInstance();
        }
        
        public boolean isInitialized() {
          return result.isInitialized();
        }
        public org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data build() {
          if (result != null && !isInitialized()) {
            throw newUninitializedMessageException(result);
          }
          return buildPartial();
        }
        
        private org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data buildParsed()
            throws com.google.protobuf.InvalidProtocolBufferException {
          if (!isInitialized()) {
            throw newUninitializedMessageException(
              result).asInvalidProtocolBufferException();
          }
          return buildPartial();
        }
        
        public org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data buildPartial() {
          if (result == null) {
            throw new IllegalStateException(
              "build() has already been called on this Builder.");
          }
          org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data returnMe = result;
          result = null;
          return returnMe;
        }
        
        public Builder mergeFrom(com.google.protobuf.Message other) {
          if (other instanceof org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data) {
            return mergeFrom((org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data)other);
          } else {
            super.mergeFrom(other);
            return this;
          }
        }
        
        public Builder mergeFrom(org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data other) {
          if (other == org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data.getDefaultInstance()) return this;
          if (other.hasKey()) {
            setKey(other.getKey());
          }
          if (other.hasValue()) {
            setValue(other.getValue());
          }
          this.mergeUnknownFields(other.getUnknownFields());
          return this;
        }
        
        public Builder mergeFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
          com.google.protobuf.UnknownFieldSet.Builder unknownFields =
            com.google.protobuf.UnknownFieldSet.newBuilder(
              this.getUnknownFields());
          while (true) {
            int tag = input.readTag();
            switch (tag) {
              case 0:
                this.setUnknownFields(unknownFields.build());
                return this;
              default: {
                if (!parseUnknownField(input, unknownFields,
                                       extensionRegistry, tag)) {
                  this.setUnknownFields(unknownFields.build());
                  return this;
                }
                break;
              }
              case 10: {
                setKey(input.readString());
                break;
              }
              case 18: {
                setValue(input.readString());
                break;
              }
            }
          }
        }
        
        
        // required string key = 1;
        public boolean hasKey() {
          return result.hasKey();
        }
        public java.lang.String getKey() {
          return result.getKey();
        }
        public Builder setKey(java.lang.String value) {
          if (value == null) {
    throw new NullPointerException();
  }
  result.hasKey = true;
          result.key_ = value;
          return this;
        }
        public Builder clearKey() {
          result.hasKey = false;
          result.key_ = getDefaultInstance().getKey();
          return this;
        }
        
        // optional string value = 2;
        public boolean hasValue() {
          return result.hasValue();
        }
        public java.lang.String getValue() {
          return result.getValue();
        }
        public Builder setValue(java.lang.String value) {
          if (value == null) {
    throw new NullPointerException();
  }
  result.hasValue = true;
          result.value_ = value;
          return this;
        }
        public Builder clearValue() {
          result.hasValue = false;
          result.value_ = getDefaultInstance().getValue();
          return this;
        }
        
        // @@protoc_insertion_point(builder_scope:timeline.TimeLineRecord.Data)
      }
      
      static {
        defaultInstance = new Data(true);
        org.sonatype.timeline.proto.TimeLineRecordProtos.internalForceInit();
        defaultInstance.initFields();
      }
      
      // @@protoc_insertion_point(class_scope:timeline.TimeLineRecord.Data)
    }
    
    // optional int64 timestamp = 1;
    public static final int TIMESTAMP_FIELD_NUMBER = 1;
    private boolean hasTimestamp;
    private long timestamp_ = 0L;
    public boolean hasTimestamp() { return hasTimestamp; }
    public long getTimestamp() { return timestamp_; }
    
    // optional string type = 2;
    public static final int TYPE_FIELD_NUMBER = 2;
    private boolean hasType;
    private java.lang.String type_ = "";
    public boolean hasType() { return hasType; }
    public java.lang.String getType() { return type_; }
    
    // optional string subType = 3;
    public static final int SUBTYPE_FIELD_NUMBER = 3;
    private boolean hasSubType;
    private java.lang.String subType_ = "";
    public boolean hasSubType() { return hasSubType; }
    public java.lang.String getSubType() { return subType_; }
    
    // repeated .timeline.TimeLineRecord.Data data = 4;
    public static final int DATA_FIELD_NUMBER = 4;
    private java.util.List<org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data> data_ =
      java.util.Collections.emptyList();
    public java.util.List<org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data> getDataList() {
      return data_;
    }
    public int getDataCount() { return data_.size(); }
    public org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data getData(int index) {
      return data_.get(index);
    }
    
    private void initFields() {
    }
    public final boolean isInitialized() {
      for (org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data element : getDataList()) {
        if (!element.isInitialized()) return false;
      }
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (hasTimestamp()) {
        output.writeInt64(1, getTimestamp());
      }
      if (hasType()) {
        output.writeString(2, getType());
      }
      if (hasSubType()) {
        output.writeString(3, getSubType());
      }
      for (org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data element : getDataList()) {
        output.writeMessage(4, element);
      }
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (hasTimestamp()) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt64Size(1, getTimestamp());
      }
      if (hasType()) {
        size += com.google.protobuf.CodedOutputStream
          .computeStringSize(2, getType());
      }
      if (hasSubType()) {
        size += com.google.protobuf.CodedOutputStream
          .computeStringSize(3, getSubType());
      }
      for (org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data element : getDataList()) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(4, element);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    public static org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder> {
      private org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord result;
      
      // Construct using org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.newBuilder()
      private Builder() {}
      
      private static Builder create() {
        Builder builder = new Builder();
        builder.result = new org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord();
        return builder;
      }
      
      protected org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord internalGetResult() {
        return result;
      }
      
      public Builder clear() {
        if (result == null) {
          throw new IllegalStateException(
            "Cannot call clear() after build().");
        }
        result = new org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord();
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(result);
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.getDescriptor();
      }
      
      public org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord getDefaultInstanceForType() {
        return org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.getDefaultInstance();
      }
      
      public boolean isInitialized() {
        return result.isInitialized();
      }
      public org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord build() {
        if (result != null && !isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return buildPartial();
      }
      
      private org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        if (!isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return buildPartial();
      }
      
      public org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord buildPartial() {
        if (result == null) {
          throw new IllegalStateException(
            "build() has already been called on this Builder.");
        }
        if (result.data_ != java.util.Collections.EMPTY_LIST) {
          result.data_ =
            java.util.Collections.unmodifiableList(result.data_);
        }
        org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord returnMe = result;
        result = null;
        return returnMe;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord) {
          return mergeFrom((org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord other) {
        if (other == org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.getDefaultInstance()) return this;
        if (other.hasTimestamp()) {
          setTimestamp(other.getTimestamp());
        }
        if (other.hasType()) {
          setType(other.getType());
        }
        if (other.hasSubType()) {
          setSubType(other.getSubType());
        }
        if (!other.data_.isEmpty()) {
          if (result.data_.isEmpty()) {
            result.data_ = new java.util.ArrayList<org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data>();
          }
          result.data_.addAll(other.data_);
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }
      
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder(
            this.getUnknownFields());
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              this.setUnknownFields(unknownFields.build());
              return this;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                this.setUnknownFields(unknownFields.build());
                return this;
              }
              break;
            }
            case 8: {
              setTimestamp(input.readInt64());
              break;
            }
            case 18: {
              setType(input.readString());
              break;
            }
            case 26: {
              setSubType(input.readString());
              break;
            }
            case 34: {
              org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data.Builder subBuilder = org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data.newBuilder();
              input.readMessage(subBuilder, extensionRegistry);
              addData(subBuilder.buildPartial());
              break;
            }
          }
        }
      }
      
      
      // optional int64 timestamp = 1;
      public boolean hasTimestamp() {
        return result.hasTimestamp();
      }
      public long getTimestamp() {
        return result.getTimestamp();
      }
      public Builder setTimestamp(long value) {
        result.hasTimestamp = true;
        result.timestamp_ = value;
        return this;
      }
      public Builder clearTimestamp() {
        result.hasTimestamp = false;
        result.timestamp_ = 0L;
        return this;
      }
      
      // optional string type = 2;
      public boolean hasType() {
        return result.hasType();
      }
      public java.lang.String getType() {
        return result.getType();
      }
      public Builder setType(java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  result.hasType = true;
        result.type_ = value;
        return this;
      }
      public Builder clearType() {
        result.hasType = false;
        result.type_ = getDefaultInstance().getType();
        return this;
      }
      
      // optional string subType = 3;
      public boolean hasSubType() {
        return result.hasSubType();
      }
      public java.lang.String getSubType() {
        return result.getSubType();
      }
      public Builder setSubType(java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  result.hasSubType = true;
        result.subType_ = value;
        return this;
      }
      public Builder clearSubType() {
        result.hasSubType = false;
        result.subType_ = getDefaultInstance().getSubType();
        return this;
      }
      
      // repeated .timeline.TimeLineRecord.Data data = 4;
      public java.util.List<org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data> getDataList() {
        return java.util.Collections.unmodifiableList(result.data_);
      }
      public int getDataCount() {
        return result.getDataCount();
      }
      public org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data getData(int index) {
        return result.getData(index);
      }
      public Builder setData(int index, org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data value) {
        if (value == null) {
          throw new NullPointerException();
        }
        result.data_.set(index, value);
        return this;
      }
      public Builder setData(int index, org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data.Builder builderForValue) {
        result.data_.set(index, builderForValue.build());
        return this;
      }
      public Builder addData(org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data value) {
        if (value == null) {
          throw new NullPointerException();
        }
        if (result.data_.isEmpty()) {
          result.data_ = new java.util.ArrayList<org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data>();
        }
        result.data_.add(value);
        return this;
      }
      public Builder addData(org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data.Builder builderForValue) {
        if (result.data_.isEmpty()) {
          result.data_ = new java.util.ArrayList<org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data>();
        }
        result.data_.add(builderForValue.build());
        return this;
      }
      public Builder addAllData(
          java.lang.Iterable<? extends org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data> values) {
        if (result.data_.isEmpty()) {
          result.data_ = new java.util.ArrayList<org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data>();
        }
        super.addAll(values, result.data_);
        return this;
      }
      public Builder clearData() {
        result.data_ = java.util.Collections.emptyList();
        return this;
      }
      
      // @@protoc_insertion_point(builder_scope:timeline.TimeLineRecord)
    }
    
    static {
      defaultInstance = new TimeLineRecord(true);
      org.sonatype.timeline.proto.TimeLineRecordProtos.internalForceInit();
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:timeline.TimeLineRecord)
  }
  
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_timeline_TimeLineRecord_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_timeline_TimeLineRecord_fieldAccessorTable;
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_timeline_TimeLineRecord_Data_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_timeline_TimeLineRecord_Data_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n$resources/proto/timelinerecord.proto\022\010" +
      "timeline\"\223\001\n\016TimeLineRecord\022\021\n\ttimestamp" +
      "\030\001 \001(\003\022\014\n\004type\030\002 \001(\t\022\017\n\007subType\030\003 \001(\t\022+\n" +
      "\004data\030\004 \003(\0132\035.timeline.TimeLineRecord.Da" +
      "ta\032\"\n\004Data\022\013\n\003key\030\001 \002(\t\022\r\n\005value\030\002 \001(\tB3" +
      "\n\033org.sonatype.timeline.protoB\024TimeLineR" +
      "ecordProtos"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_timeline_TimeLineRecord_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_timeline_TimeLineRecord_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_timeline_TimeLineRecord_descriptor,
              new java.lang.String[] { "Timestamp", "Type", "SubType", "Data", },
              org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.class,
              org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Builder.class);
          internal_static_timeline_TimeLineRecord_Data_descriptor =
            internal_static_timeline_TimeLineRecord_descriptor.getNestedTypes().get(0);
          internal_static_timeline_TimeLineRecord_Data_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_timeline_TimeLineRecord_Data_descriptor,
              new java.lang.String[] { "Key", "Value", },
              org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data.class,
              org.sonatype.timeline.proto.TimeLineRecordProtos.TimeLineRecord.Data.Builder.class);
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }
  
  public static void internalForceInit() {}
  
  // @@protoc_insertion_point(outer_class_scope)
}
