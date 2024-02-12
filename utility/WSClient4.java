package utility;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.wsdl.PortTypeImpl;

public class WSClient4 {
	public static Document callWebService(String wsdl, String opName, Element input) throws Exception {

		try {
			WSDLFactory sf = WSDLFactory.newInstance();
			WSDLReader s = sf.newWSDLReader();
			Definition wsdlDefinition = s.readWSDL(null, wsdl);

			// To get the targetNamespace from WSDL
			Map obNAME = wsdlDefinition.getNamespaces();
			// iterate over the Map
			Iterator entries = obNAME.entrySet().iterator();
			String nsFromWsdl = null;
			while (entries.hasNext()) {
				Map.Entry entry = (Map.Entry) entries.next();
				if (entry.getKey().equals("ns"))
					nsFromWsdl = (String) entry.getValue();
			}

			// Then create a port type implementation instance which has the
			// information we need
			Object[] ob = wsdlDefinition.getPortTypes().values().toArray();
			PortTypeImpl pti = (PortTypeImpl) ob[0];

			// Now we can get the name of operations
			pti.getQName().getNamespaceURI();
			List list = pti.getOperations();
			Iterator operIter = list.iterator();

			RPCServiceClient serviceClient = null;
			// public class RPCServiceClient extends ServiceClient
			try {
				serviceClient = new RPCServiceClient();
			} catch (AxisFault e1) {
				e1.printStackTrace();
			}

			Options options = serviceClient.getOptions();
			// getOptions method of ServiceClient gets the basic client
			// configuration
			// this is empty, so we set it in the following
			EndpointReference targetEPR = new EndpointReference(wsdl);

			// Set WS-Addressing Action / SOAP Action string.
			options.setAction(opName);
			// Set WS-Addressing To endpoint.
			options.setTo(targetEPR);

			OMElement xmlPayload = constructPayload(wsdl, opName, input);
			// /////////////////
			OMElement response = serviceClient.sendReceive(xmlPayload);
			// if output is a complex type, return its DOM representation:
			if (response instanceof OMElementImpl) {
				System.out.println("yes");
				String xmlString = ((OMElementImpl) response).toString();
				return XMLParser.getDocument(xmlString);
			}

		} catch (Exception ex) {
			System.out.println(ex);
		}
		System.out.println("WSClient returning null");
		return null;
	}

	public static OMElement constructPayload(String wsdl, String opName, Element input) throws Exception {
		WSDLFactory sf = WSDLFactory.newInstance();
		WSDLReader s = sf.newWSDLReader();
		Definition wsdlDefinition = s.readWSDL(null, wsdl);

		// To get the targetNamespace from WSDL
		Map obNAME = wsdlDefinition.getNamespaces();
		// iterate over the Map
		Iterator entries = obNAME.entrySet().iterator();
		String nsFromWsdl = null;
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
			if (entry.getKey().equals("ns"))
				nsFromWsdl = (String) entry.getValue();
		}

		String payloadStr = Utility.nodeToString(input).replace("<wrapperElement>", "").replace("</wrapperElement>", "");
		
//		payloadStr = "<" + opName + " xmlns=\"" + nsFromWsdl + "\">"
//				+ payloadStr;
//		payloadStr += ("</" + opName + ">");
		return AXIOMUtil.stringToOM(payloadStr);
	}

}
