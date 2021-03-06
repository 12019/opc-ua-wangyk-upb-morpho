/* ========================================================================
 * Copyright (c) 2005-2010 The OPC Foundation, Inc. All rights reserved.
 *
 * OPC Reciprocal Community License ("RCL") Version 1.00
 * 
 * Unless explicitly acquired and licensed from Licensor under another 
 * license, the contents of this file are subject to the Reciprocal 
 * Community License ("RCL") Version 1.00, or subsequent versions as 
 * allowed by the RCL, and You may not copy or use this file in either 
 * source code or executable form, except in compliance with the terms and 
 * conditions of the RCL.
 * 
 * All software distributed under the RCL is provided strictly on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, 
 * AND LICENSOR HEREBY DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT 
 * LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the RCL for specific 
 * language governing rights and limitations under the RCL.
 *
 * The complete license agreement can be found here:
 * http://opcfoundation.org/License/RCL/1.00/
 * ======================================================================*/

package org.opcfoundation.ua.encoding.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.encoding.DecodingException;
import org.opcfoundation.ua.encoding.EncodeType;
import org.opcfoundation.ua.encoding.EncodingException;
import org.opcfoundation.ua.encoding.IDecoder;
import org.opcfoundation.ua.encoding.IEncodeable;
import org.opcfoundation.ua.encoding.IEncoder;
import org.opcfoundation.ua.encoding.binary.IEncodeableSerializer;

/**
 *
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public class SerializerComposition implements IEncodeableSerializer {

	Map<Class<? extends IEncodeable>, IEncodeableSerializer> serializers = new HashMap<Class<? extends IEncodeable>, IEncodeableSerializer>();
	Map<NodeId, Class<? extends IEncodeable>> idToClass = new HashMap<NodeId, Class<? extends IEncodeable>>();
	Map<Class<? extends IEncodeable>, NodeId> classToBinId = new HashMap<Class<? extends IEncodeable>, NodeId>();
	Map<Class<? extends IEncodeable>, NodeId> classToXmlId = new HashMap<Class<? extends IEncodeable>, NodeId>();
	Set<NodeId> nodeIds = idToClass.keySet();
	Set<Class<? extends IEncodeable>> classes = classToBinId.keySet();

	public SerializerComposition() {
	}
	
	public void addSerializer(IEncodeableSerializer serializer)
	{
		List<Class<? extends IEncodeable>> classes = new ArrayList<Class<? extends IEncodeable>>();
		serializer.getSupportedClasses(classes);
		for (Class<? extends IEncodeable> clazz : classes)
		{
			assert(classToBinId.get(clazz)==null);
			NodeId binId = serializer.getNodeId(clazz, EncodeType.Binary);
			classToBinId.put(clazz, binId);
			NodeId xmlId = serializer.getNodeId(clazz, EncodeType.Xml);
			classToXmlId.put(clazz, xmlId);
			serializers.put(clazz, serializer);
			if (binId!=null) 
				idToClass.put(binId, clazz);			
			if (xmlId!=null) 
				idToClass.put(xmlId, clazz);			
		}
	}

	public void putEncodeable(Class<? extends IEncodeable> clazz, IEncodeable encodeable, IEncoder encoder) throws EncodingException {
		IEncodeableSerializer s = serializers.get(clazz);
		if (s==null) throw new EncodingException("Cannot encode "+clazz);
		s.putEncodeable(clazz, encodeable, encoder);
	}
	
	public void calcEncodeable(Class<? extends IEncodeable> clazz, IEncodeable encodeable, IEncoder calculator)
	throws EncodingException {
		IEncodeableSerializer s = serializers.get(clazz);
		if (s==null) throw new EncodingException("Cannot encode "+clazz);
		s.calcEncodeable(clazz, encodeable, calculator);		
	}

	public IEncodeable getEncodeable(Class<? extends IEncodeable> clazz, IDecoder decoder) throws DecodingException {
		IEncodeableSerializer s = serializers.get(clazz);
		if (s==null) throw new DecodingException("Cannot decode "+clazz);
		return s.getEncodeable(clazz, decoder);
	}

	public Class<? extends IEncodeable> getClass(NodeId id) {
		return idToClass.get(id);
	}
	
	public NodeId getNodeId(Class<? extends IEncodeable> clazz, EncodeType type) {
		if (type == EncodeType.Binary)
			return classToBinId.get(clazz);
		if (type == EncodeType.Xml)
			return classToXmlId.get(clazz);
		return null;
	}

	public void getSupportedClasses(Collection<Class<? extends IEncodeable>> result) {
		result.addAll(classes);
	}

	public void getSupportedNodeIds(Collection<NodeId> result) {
		result.addAll(nodeIds);
	}

}
