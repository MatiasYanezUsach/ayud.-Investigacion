package model;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import org.apache.commons.math3.util.Precision;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PDPInstance {
    private int nNodes; //NUMERO TOTAL DE NODOS DE LA INSTANCIA
    private List<ArrayList<Float>> costMatrix; // MATRIZ DE COSTOS DE LA INSTANCIA
    private List<Float> deliveryQuantities; //LISTA DE CANTIDADES A ENTREGAR DE CADA NODO
    private List<Float> pickupQuantities; //LISTA DE CANTIDADES A RECOGER DE CADA NODO
    private List<Float> vehicleCapacities; //LISTA DE CAPACIDADES DE CADA VEHICULO
    private Float vehicleCapacity; //CAPACIDAD DE LOS VEHICULOS
    private List<ArrayList<Float>> currentVehiclesLoadNodes; //LISTA DE CAPACIDADES ACTUAL DE CADA VEHICULO
    private List<Float> currentDeliveryQuantities; // LISTA DE CANTIDADES A ENTREGAR ACTUAL DE CADA VEHICULO
    private List<Float> currentPickupQuantities; // LISTA DE CANTIDADES A RECOGER ACTUAL DE CADA VEHICULO
    private List<ArrayList<Integer>> routes; // (SOLUCION) MATRIZ DE RUTAS, CADA ELEMENTO DE LA LISTA CONTIENE UNA LISTAS QUE REPRESENTA LA RUTA DE CADA VEHICULO
    private List<Integer> visited;
    private List<Integer> notVisited;
    private double totalCost;
    private double time;
    private boolean closed = false;
    private boolean lastChange = false;
    private boolean optimal = false;
    private boolean cplexLastChange;

    public PDPInstance() {
        this.nNodes = 0;
        this.costMatrix = new ArrayList<>();
        this.deliveryQuantities = new ArrayList<>();
        this.pickupQuantities = new ArrayList<>();
        //this.vehicleCapacities = new ArrayList<>();
        this.vehicleCapacity = 0.0F;
        this.currentVehiclesLoadNodes = new ArrayList<>();
        this.currentDeliveryQuantities = new ArrayList<>();
        this.currentPickupQuantities = new ArrayList<>();
        this.routes = new ArrayList<>();
        this.visited = new ArrayList<>();
        this.notVisited = new ArrayList<>();
        this.totalCost = Precision.round(0, 2);

    }

    float getMaxCost() {
        float maxCost = 0;
        for (int i = 0; i < costMatrix.size(); i++) {
            for (int j = 0; j < costMatrix.get(i).size(); j++) {
                if (costMatrix.get(i).get(j) > maxCost) {
                    maxCost = costMatrix.get(i).get(j);
                }
            }
        }
        return 4*maxCost;
    }

    public PDPInstance(PDPInstance pdpInstance) {
        this.nNodes = pdpInstance.getnNodes();

        this.costMatrix = new ArrayList<>();
        for (ArrayList costList : pdpInstance.getCostMatrix()) {
            ArrayList<Float> costListCopy = new ArrayList<>(costList);
            this.costMatrix.add(costListCopy);
        }

        this.deliveryQuantities = new ArrayList<>(pdpInstance.getDeliveryQuantities());

        this.pickupQuantities = new ArrayList<>(pdpInstance.getPickupQuantities());

        //this.vehicleCapacities = new ArrayList<>(pdpInstance.getVehicleCapacities());

        this.vehicleCapacity = pdpInstance.getVehicleCapacity();

        this.currentVehiclesLoadNodes = new ArrayList<>();
        for (ArrayList currentVehicleLoadNode : pdpInstance.getCurrentVehiclesLoadNodes()) {
            ArrayList<Float> currentVehicleLoadNodeCopy = new ArrayList<>(currentVehicleLoadNode);
            this.currentVehiclesLoadNodes.add(currentVehicleLoadNodeCopy);
        }

        this.currentDeliveryQuantities = new ArrayList<>(pdpInstance.getCurrentDeliveryQuantities());
        this.currentPickupQuantities = new ArrayList<>(pdpInstance.getCurrentPickupQuantities());

        this.routes = new ArrayList<>();
        for (ArrayList route : pdpInstance.getRoutes()) {
            ArrayList<Integer> routeCopy = new ArrayList<>(route);
            this.routes.add(routeCopy);
        }

        this.visited = new ArrayList<>(pdpInstance.getVisited());
        this.notVisited = new ArrayList<>(pdpInstance.getNotVisited());
        this.totalCost = pdpInstance.getTotalCost();
    }

    private static ArrayList<Integer> createNewRoute() {
        ArrayList<Integer> route = new ArrayList<>();
        route.add(0);
        route.add(0);
        return route;
    }

    public static PDPInstance createRandomPDP(int size, int seed) {
        PDPInstance randomPDP = new PDPInstance();

        randomPDP.setnNodes(size);

        //MATRIZ DE COSTO
        List<ArrayList<Float>> costMatrix = new ArrayList<ArrayList<Float>>();
        Random r = new Random(seed);
        for (int i = 0; i < size; i++) {
            ArrayList<Float> costList = new ArrayList<Float>();
            for (int j = 0; j < size; j++) {
                costList.add((float) r.nextInt(20) + 1);
            }
            costMatrix.add(costList);

        }

        for (int i = 0; i < size; i++) {
            costMatrix.get(i).set(i, 0.0F);
        }
        randomPDP.setCostMatrix(costMatrix);

        //CANTIDADES A LLEVAR
        List<Float> deliveryQuantities = new ArrayList<>();
        for (int i = 0; i < size - 1; i++) {
            deliveryQuantities.add((float) r.nextInt(20));
        }
        randomPDP.setDeliveryQuantities(deliveryQuantities);

        //CANTIDADES A RECOGER
        List<Float> pickupQuantities = new ArrayList<>();
        for (int i = 0; i < size - 1; i++) {
            pickupQuantities.add((float) r.nextInt(20));
        }
        randomPDP.setPickupQuantities(pickupQuantities);

        //CAPACIDADES VEHICULOS
        List<Float> vehicleCapacities = new ArrayList<>();
        for (int i = 0; i < size / 2; i++) {
            vehicleCapacities.add((float) r.nextInt(30) + 20);
        }
        randomPDP.setVehicleCapacity(vehicleCapacities.get(0));

        //RUTAS EN 0
        List<ArrayList<Integer>> routes = new ArrayList<>();
        ArrayList<Integer> route = createNewRoute();
        routes.add(route);
        randomPDP.setRoutes(routes);

        //NODOS VISITADOS
        ArrayList<Integer> visited = new ArrayList<>();
        visited.add(0);
        randomPDP.setVisited(visited);

        //NODOS NO VISITADOS
        ArrayList<Integer> notVisited = new ArrayList<>();
        for (int i = 1; i < size; i++) {
            notVisited.add(i);
        }
        randomPDP.setNotVisited(notVisited);

        //COSTO TOTAL
        randomPDP.setTotalCost(0);

        //CANTIDAD DE CARGA A ENTREGAR DE CAD VEHICULO (PARTE EN 0 Y VA CAMBIANDO AL IR ARMANDO LA RUTA)
        List<Float> currentDeliveryQuantities = new ArrayList<>();
        currentDeliveryQuantities.add(0.0F);

        randomPDP.setCurrentDeliveryQuantities(currentDeliveryQuantities);

        //CANTIDAD DE CARGA A RECOGER DE CAD VEHICULO (PARTE EN 0 Y VA CAMBIANDO AL IR ARMANDO LA RUTA)
        List<Float> currentPickupQuantities = new ArrayList<>();

        currentPickupQuantities.add(0.0F);

        randomPDP.setCurrentPickupQuantities(currentPickupQuantities);

        //CAPACIDAD ACTUIAL DE CADA VEHICULO EN CADA PUNTO DE LA RUTA
        List<ArrayList<Float>> currentVehiclesLoadNodes = new ArrayList<ArrayList<Float>>();
        ArrayList<Float> currentVehicleLoadNodes = new ArrayList<>();
        currentVehicleLoadNodes.add((float) 0);
        currentVehiclesLoadNodes.add(currentVehicleLoadNodes);

        randomPDP.setCurrentVehiclesLoadNodes(currentVehiclesLoadNodes);


        return randomPDP;

    }

    public double getCurrentProfit() {
        return getTotalCost();
    }

    public List<ArrayList<Integer>> getSol() {
        return getRoutes();
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public Float getVehicleCapacity() {
        return vehicleCapacity;
    }

    public void setVehicleCapacity(Float vehicleCapacity) {
        this.vehicleCapacity = vehicleCapacity;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public boolean isOptimal() {
        return optimal;
    }

    public void setOptimal(boolean optimal) {
        this.optimal = optimal;
    }

    public int getnNodes() {
        return nNodes;
    }

    public void setnNodes(int nNodes) {
        this.nNodes = nNodes;
    }

    public List<ArrayList<Float>> getCostMatrix() {
        return costMatrix;
    }

    public void setCostMatrix(List<ArrayList<Float>> costMatrix) {
        this.costMatrix = costMatrix;
    }

    public List<Float> getDeliveryQuantities() {
        return deliveryQuantities;
    }

    public void setDeliveryQuantities(List<Float> deliveryQuantities) {
        this.deliveryQuantities = deliveryQuantities;
    }

    public List<Float> getPickupQuantities() {
        return pickupQuantities;
    }

    public void setPickupQuantities(List<Float> pickupQuantities) {
        this.pickupQuantities = pickupQuantities;
    }

    public List<Float> getVehicleCapacities() {
        return vehicleCapacities;
    }

    public void setVehicleCapacities(List<Float> vehicleCapacities) {
        this.vehicleCapacities = vehicleCapacities;
    }

    public List<ArrayList<Integer>> getRoutes() {
        return routes;
    }

    public void setRoutes(List<ArrayList<Integer>> routes) {
        this.routes = routes;
    }

    public List<Integer> getVisited() {
        return visited;
    }

    public void setVisited(List<Integer> visited) {
        this.visited = visited;
    }

    public List<Integer> getNotVisited() {
        return notVisited;
    }

    public void setNotVisited(List<Integer> notVisited) {
        this.notVisited = notVisited;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = Precision.round(totalCost, 2);
    }

    public List<Float> getCurrentDeliveryQuantities() {
        return currentDeliveryQuantities;
    }

    public void setCurrentDeliveryQuantities(List<Float> currentDeliveryQuantities) {
        this.currentDeliveryQuantities = currentDeliveryQuantities;
    }

    public List<Float> getCurrentPickupQuantities() {
        return currentPickupQuantities;
    }

    public void setCurrentPickupQuantities(List<Float> currentPickupQuantities) {
        this.currentPickupQuantities = currentPickupQuantities;
    }

    public PDPInstance clone() {
        return new PDPInstance(this);
    }

    public List<ArrayList<Float>> getCurrentVehiclesLoadNodes() {
        return currentVehiclesLoadNodes;
    }

    public void setCurrentVehiclesLoadNodes(List<ArrayList<Float>> currentVehiclesLoadNodes) {
        this.currentVehiclesLoadNodes = currentVehiclesLoadNodes;
    }

    public List<ArrayList<Integer>> getRoutesCopy() {
        List<ArrayList<Integer>> routesCopy = new ArrayList<>();
        for (ArrayList route : routes) {
            ArrayList<Integer> routeCopy = new ArrayList<>(route);
            routesCopy.add(routeCopy);
        }
        return routesCopy;
    }

    public List<ArrayList<Integer>> getRoutesCopy(List<ArrayList<Integer>> routes) {
        List<ArrayList<Integer>> routesCopy = new ArrayList<>();
        for (ArrayList route : routes) {
            ArrayList<Integer> routeCopy = new ArrayList<>(route);
            routesCopy.add(routeCopy);
        }
        return routesCopy;
    }

    public void updatePDPInstanceFromRead(List<ArrayList<Float>> costMatrix, List<Float> deliveryQuantities, List<Float> pickupQuantities, Float vehicleCapacity) {
        this.costMatrix = costMatrix;
        this.nNodes = costMatrix.size();
        this.deliveryQuantities = deliveryQuantities;
        this.pickupQuantities = pickupQuantities;
        //this.vehicleCapacities = vehicleCapacities;
        this.vehicleCapacity = vehicleCapacity;

        //RUTAS EN 0
        List<ArrayList<Integer>> routes = new ArrayList<>();
        ArrayList<Integer> route = createNewRoute();
        routes.add(route);
        this.routes = routes;

        //NODOS VISITADOS
        ArrayList<Integer> visited = new ArrayList<>();
        visited.add(0);
        this.visited = visited;

        //NODOS NO VISITADOS
        ArrayList<Integer> notVisited = new ArrayList<>();
        for (int i = 1; i < costMatrix.size(); i++) {
            notVisited.add(i);
        }

        this.notVisited = notVisited;

        //COSTO TOTAL
        this.totalCost = Precision.round(0, 2);

        //CANTIDAD DE CARGA A ENTREGAR DE CAD VEHICULO (PARTE EN 0 Y VA CAMBIANDO AL IR ARMANDO LA RUTA)
        List<Float> currentDeliveryQuantities = new ArrayList<>();
        currentDeliveryQuantities.add(0.0F);
        this.currentDeliveryQuantities = currentDeliveryQuantities;

        //CANTIDAD DE CARGA A RECOGER DE CAD VEHICULO (PARTE EN 0 Y VA CAMBIANDO AL IR ARMANDO LA RUTA)
        List<Float> currentPickupQuantities = new ArrayList<>();
        currentPickupQuantities.add(0.0F);
        this.currentPickupQuantities = currentPickupQuantities;

        //CAPACIDAD ACTUAL DE CADA VEHICULO EN CADA PUNTO DE LA RUTA
        List<ArrayList<Float>> currentVehiclesLoadNodes = new ArrayList<>();
        ArrayList<Float> currentVehicleLoadNodes = new ArrayList<>();
        currentVehicleLoadNodes.add(0.0F);
        currentVehicleLoadNodes.add(0.0F);
        currentVehiclesLoadNodes.add(currentVehicleLoadNodes);

        this.currentVehiclesLoadNodes = currentVehiclesLoadNodes;
    }

    //RESTRICCION QUE VERIFICA QUE LA CARGA TOTAL A RECOGER NO SUPERE LA CAPACIDAD DEL VEHICULO (AL AGREGAR UN NUEVO NODO)
    private boolean pickupRestriction(Integer routeIndex, Integer node) {
        float currentPickupQuantity = this.currentPickupQuantities.get(routeIndex);
        float nodePickupQuantity = this.pickupQuantities.get(node - 1);

        return this.vehicleCapacity >= (currentPickupQuantity + nodePickupQuantity);
    }

    //RESTRICCION QUE VERIFICA QUE LA CARGA TOTAL A ENTREGAR NO SUPERE LA CAPACIDAD DEL VEHICULO (AL AGREGAR UN NUEVO NODO)
    private boolean deliveryRestriction(Integer routeIndex, Integer node) {
        return this.vehicleCapacity >= (this.currentDeliveryQuantities.get(routeIndex) + this.deliveryQuantities.get(node - 1));
    }

    //RESTRICCION QUE VERIFICA QUE LA CARGA DE CADA CAMION NO SE VEA SUPERADA EN NINGUN PUNTO DE LA RUTA (AL AGREGAR UN NODO)
    private boolean intermediateRestriction(Integer routeIndex, Integer node, Integer position) {


        //SE OBTIENEN LAS CARGAS DEL VEHICULO  EN CADA NODO
        List<Float> vehicleLoadNodes = this.currentVehiclesLoadNodes.get(routeIndex);
        //SE OBTIENE LA CARGA A RECOGER DEL NUEVO NODO
        Float pickupQuantity = this.pickupQuantities.get(node - 1);
        //SE OBTIENE LA CARGA A ENTREGAR DEL NUEVO NODO
        Float deliveryQuantity = this.deliveryQuantities.get(node - 1);

        //SE VERIFICA QUE LA CARGA A RECOGER NO SUPERE LA CAPACIDAD DEL VEHICULO
        for (int i = position; i < vehicleLoadNodes.size(); i++) {
            if (vehicleLoadNodes.get(i) + pickupQuantity > this.vehicleCapacity) {
                return false;
            }


        }


        //SE VERIFICA QUE LA CARGA A ENTREGAR NO SUPERE LA CAPACIDAD DEL VEHICULO EN CADA NODO QUE VISITA
        for (int i = 0; i < position; i++) {
            if (vehicleLoadNodes.get(i) + deliveryQuantity > this.vehicleCapacity) {
                return false;
            }
        }

        return true;
    }

    //FUNCION QUE ACTUALIZA LA CARGA ACUTAL EN CADA NODO DE UN VEHICULO
    private void updateCurrentVehiclesLoadNodes(Integer routeIndex, Integer node, Integer position) {


        if (routeIndex == currentVehiclesLoadNodes.size()) {
            ArrayList<Float> newVehicleLoadNodes = new ArrayList<>();
            newVehicleLoadNodes.add(0.0F);
            newVehicleLoadNodes.add(0.0F);
            currentVehiclesLoadNodes.add(newVehicleLoadNodes);
        }

        List<Float> vehicleLoadNodes = this.currentVehiclesLoadNodes.get(routeIndex);
        Float pickupQuantity = this.pickupQuantities.get(node - 1);
        Float deliveryQuantity = this.deliveryQuantities.get(node - 1);


        for (int i = 0; i < position; i++) {
            vehicleLoadNodes.set(i, vehicleLoadNodes.get(i) + deliveryQuantity);
        }

        vehicleLoadNodes.add(position, vehicleLoadNodes.get(position - 1) + pickupQuantity - deliveryQuantity);

        for (int i = position + 1; i < vehicleLoadNodes.size(); i++) {
            vehicleLoadNodes.set(i, vehicleLoadNodes.get(i) + pickupQuantity);
        }

    }

    //FUNCION QUE ACTUALIZA LA CARGA A RECOGER DE UN VEHICULO
    private void updateCurrentPickupQuantities(Integer routeIndex, Integer node) {

        if (routeIndex == currentPickupQuantities.size()) {
            this.currentPickupQuantities.add(routeIndex, this.pickupQuantities.get(node - 1));
        } else {
            this.currentPickupQuantities.set(routeIndex, this.currentPickupQuantities.get(routeIndex) + this.pickupQuantities.get(node - 1));
        }

    }

    //FUNCION QUE CIERRA LAS RUTAS (AGREGA EL DEPOSITO COMO NODO FINAL EN CADA RUTA)

    //FUNCION QUE ACTUALIZA LA CARGA A ENTREGAR DE UN VEHICULO
    private void updateCurrentDeliveryQuantities(Integer routeIndex, Integer node) {
        if (routeIndex == currentDeliveryQuantities.size()) {
            this.currentDeliveryQuantities.add(routeIndex, this.deliveryQuantities.get(node - 1));
        } else {
            this.currentDeliveryQuantities.set(routeIndex, this.currentDeliveryQuantities.get(routeIndex) + this.deliveryQuantities.get(node - 1));
        }

    }

    //HEURISTICA DE QUE AÑADE EL NODO DE MENOS COSTO AL ULTIMO NODO AGREGADO DE CUALQUIERA DE LAS RUTAS O LO AGREGA UNA RUTA NUEVA EN CASO EN CASO DE SER MENOS COSTOSO
    public boolean add_nearest_neighbor_last_node() {
        resetLastchanges();
        if (optimal) {
            return false;
        }
        float bestCost = Float.MAX_VALUE;
        Integer bestNeighbor = -1; //SE COMIENZA CON UN VALOR NO VALIDO, PARA LUEGO ACTUALIZARLO CON EL DEL MEJOR VECINO
        ArrayList<Integer> bestRoute = null; //SE GUARDA LA RUTA EN LA QUE SE AGREGA EL MEJOR VECINO
        boolean newRouteFlag = false;

        //SE VERIFICA QUE QUEDEN NODOS POR VISITAR
        if (notVisited.size() > 0) {
            //SE BUSCA EL NODO NO VISITADO QUE AGREGUE EL MENOR COSTO A ALGUNA DE LAS RUTAS DE LA SOLUCION
            for (Integer notVisitedNode : notVisited) {
                for (ArrayList route : this.routes) {
                    //System.out.println("dentro for nearest last node");
                    //REVISAR ESTA LINEA
                    //SE OBTIENE EL ULTIMO NODO DE LA RUTA
                    Integer penultimateNode = (Integer) route.get(route.size() - 2);
                    Integer lastNode = (Integer) route.get(route.size() - 1);


                    //SE OBTIENE EL COSTO DE IR DEL ULTIMO NODO DE LA RUTA AL NODO NO VISITADO QUE SE ESTA REVISANDO
                    float cost = costMatrix.get(penultimateNode).get(notVisitedNode) + costMatrix.get(notVisitedNode).get(lastNode) - costMatrix.get(penultimateNode).get(lastNode);

                    int routeIndex = this.routes.indexOf(route);

                    //SI EL COSTO DE IR AL NODO QUE SE ESTA REVISANDO ES MENOR QUE EL MEJOR COSTO ACTUAL
                    //SE VERIFICA QUE SE CUMPLAN LAS RESTRICCIONES DE CAPACIDAD
                    //SI SE CUMPLE TODO SE ACTUALIZA MEJOR VECINO
                    if (cost < bestCost
                            && pickupRestriction(routeIndex, notVisitedNode)
                            && deliveryRestriction(routeIndex, notVisitedNode)
                            && intermediateRestriction(routeIndex, notVisitedNode, route.size() - 2)
                    ) {
                        bestCost = cost;
                        bestNeighbor = notVisitedNode;
                        bestRoute = route;
                        newRouteFlag = false;
                    }
                }

                ArrayList<Integer> newRoute = createNewRoute();
                Integer penultimateNode = newRoute.get(newRoute.size() - 2);
                Integer lastNode = newRoute.get(newRoute.size() - 1);

                float cost = costMatrix.get(penultimateNode).get(notVisitedNode) + costMatrix.get(notVisitedNode).get(lastNode) - costMatrix.get(penultimateNode).get(lastNode);
                if (cost < bestCost) {
                    bestCost = cost;
                    bestNeighbor = notVisitedNode;
                    bestRoute = newRoute;
                    newRouteFlag = true;
                    //AÑADIR RUTA NUEVA A LSITA DE RUTAS
                    //this.routes.add(bestRoute);
                }


            }

            if (newRouteFlag) {
                this.routes.add(bestRoute);
            }

            if (bestRoute == null) {
                return false;
            }
            //SE AÑADE EL MEJOR NODO SIN VISITAR A LA RUTA
            bestRoute.add(bestRoute.size() - 1, bestNeighbor);
            int routeIndex = this.routes.indexOf(bestRoute);
            //SE SUMA EL COSTO DE VISITAR EL NODO
            this.totalCost += Precision.round(bestCost, 2);
            //SE ACTUALIZA EL TOTAL DE PICKUP
            this.updateCurrentPickupQuantities(routeIndex, bestNeighbor);
            //SE ACTUALIZA EL TOTAL DE DELIVERY
            this.updateCurrentDeliveryQuantities(routeIndex, bestNeighbor);
            //SE ACTUALIZA LA CARGA ACTUAL DE CADA RUTA EN CADA NODO
            this.updateCurrentVehiclesLoadNodes(routeIndex, bestNeighbor, bestRoute.size() - 2);


            //SE QUITA ESE NODO DE LA LISTA DE NO VISITADOS
            notVisited.remove(Integer.valueOf(bestNeighbor));
            //SE ANADE ESE NODO A LA LISTA DE VISITADOS
            visited.add(bestNeighbor);
            lastChange = true;

            return true;
        }


        return false;


    }

    //HEURISTICA DE QUE AÑADE EL NODO DE MENOS COSTO AL PRIMER NODO AGREGADO DE CUALQUIERA DE LAS RUTAS O LO AGREGA UNA RUTA NUEVA EN CASO EN CASO DE SER MENOS COSTOSO
    public boolean add_nearest_neighbor_first_node() {
        resetLastchanges();
        if (optimal) {
            return false;
        }
        float bestCost = Float.MAX_VALUE;
        Integer bestNeighbor = -1; //SE COMIENZA CON UN VALOR NO VALIDO, PARA LUEGO ACTUALIZARLO CON EL DEL MEJOR VECINO
        ArrayList<Integer> bestRoute = null; //SE GUARDA LA RUTA EN LA QUE SE AGREGA EL MEJOR VECINO
        boolean newRouteFlag = false;

        //SE VERIFICA QUE QUEDEN NODOS POR VISITAR
        if (notVisited.size() > 0) {
            //SE BUSCA EL NODO NO VISITADO QUE AGREGUE EL MENOR COSTO A ALGUNA DE LAS RUTAS DE LA SOLUCION
            for (Integer notVisitedNode : notVisited) {
                for (ArrayList route : this.routes) {
                    //System.out.println("dentro for nearest last node");
                    //REVISAR ESTA LINEA
                    //SE OBTIENE EL PRIMER NODO DE LA RUTA
                    Integer secondNode = (Integer) route.get(1);
                    Integer firstNode = (Integer) route.get(0);


                    //SE OBTIENE EL COSTO DE IR DEL ULTIMO NODO DE LA RUTA AL NODO NO VISITADO QUE SE ESTA REVISANDO
                    float cost = costMatrix.get(firstNode).get(notVisitedNode) + costMatrix.get(notVisitedNode).get(secondNode) - costMatrix.get(firstNode).get(secondNode);

                    int routeIndex = this.routes.indexOf(route);

                    //SI EL COSTO DE IR AL NODO QUE SE ESTA REVISANDO ES MENOR QUE EL MEJOR COSTO ACTUAL
                    //SE VERIFICA QUE SE CUMPLAN LAS RESTRICCIONES DE CAPACIDAD
                    //SI SE CUMPLE TODO SE ACTUALIZA MEJOR VECINO
                    if (cost < bestCost
                            && pickupRestriction(routeIndex, notVisitedNode)
                            && deliveryRestriction(routeIndex, notVisitedNode)
                            && intermediateRestriction(routeIndex, notVisitedNode, 1)
                    ) {
                        bestCost = cost;
                        bestNeighbor = notVisitedNode;
                        bestRoute = route;
                        newRouteFlag = false;
                    }
                }

                ArrayList<Integer> newRoute = createNewRoute();
                Integer firstNode = newRoute.get(0);
                Integer secondNode = newRoute.get(1);

                float cost = costMatrix.get(firstNode).get(notVisitedNode) + costMatrix.get(notVisitedNode).get(secondNode) - costMatrix.get(firstNode).get(secondNode);
                if (cost < bestCost) {
                    bestCost = cost;
                    bestNeighbor = notVisitedNode;
                    bestRoute = newRoute;
                    newRouteFlag = true;
                    //AÑADIR RUTA NUEVA A LSITA DE RUTAS
                    //this.routes.add(bestRoute);
                }


            }

            if (newRouteFlag) {
                this.routes.add(bestRoute);
            }

            if (bestRoute == null) {
                return false;
            }
            //SE AÑADE EL MEJOR NODO SIN VISITAR A LA RUTA
            bestRoute.add(1, bestNeighbor);
            int routeIndex = this.routes.indexOf(bestRoute);
            //SE SUMA EL COSTO DE VISITAR EL NODO
            this.totalCost += Precision.round(bestCost, 2);
            //SE ACTUALIZA EL TOTAL DE PICKUP
            this.updateCurrentPickupQuantities(routeIndex, bestNeighbor);
            //SE ACTUALIZA EL TOTAL DE DELIVERY
            this.updateCurrentDeliveryQuantities(routeIndex, bestNeighbor);
            //SE ACTUALIZA LA CARGA ACTUAL DE CADA RUTA EN CADA NODO
            this.updateCurrentVehiclesLoadNodes(routeIndex, bestNeighbor, 1);


            //SE QUITA ESE NODO DE LA LISTA DE NO VISITADOS
            notVisited.remove(Integer.valueOf(bestNeighbor));
            //SE ANADE ESE NODO A LA LISTA DE VISITADOS
            visited.add(bestNeighbor);
            lastChange = true;

            return true;
        }


        return false;


    }

    //HEURISTICA DE QUE AÑADE EL NODO DE MAS COSTOSO AL ULTIMO NODO AGREGADO DE CUALQUIERA DE LAS RUTAS O LO AGREGA UNA RUTA NUEVA EN CASO EN CASO DE SER MAS COSTOSO
    public boolean add_farthest_neighbor_last_node() {
        resetLastchanges();
        if (optimal) {
            return false;
        }
        float worstCost = 0.0F;
        Integer bestNeighbor = -1; //SE COMIENZA CON UN VALOR NO VALIDO, PARA LUEGO ACTUALIZARLO CON EL DEL MEJOR VECINO
        ArrayList<Integer> bestRoute = null; //SE GUARDA LA RUTA EN LA QUE SE AGREGA EL MEJOR VECINO
        boolean newRouteFlag = false;

        //SE VERIFICA QUE QUEDEN NODOS POR VISITAR
        if (notVisited.size() > 0) {
            //SE BUSCA EL NODO NO VISITADO QUE AGREGUE EL MENOR COSTO A ALGUNA DE LAS RUTAS DE LA SOLUCION
            for (Integer notVisitedNode : notVisited) {
                for (ArrayList route : this.routes) {
                    //REVISAR ESTA LINEA
                    //SE OBTIENE EL ULTIMO NODO DE LA RUTA
                    Integer penultimateNode = (Integer) route.get(route.size() - 2);
                    Integer lastNode = (Integer) route.get(route.size() - 1);

                    //SE OBTIENE EL COSTO DE IR DEL ULTIMO NODO DE LA RUTA AL NODO NO VISITADO QUE SE ESTA REVISANDO
                    float cost = costMatrix.get(penultimateNode).get(notVisitedNode) + costMatrix.get(notVisitedNode).get(lastNode) - costMatrix.get(penultimateNode).get(lastNode);

                    int routeIndex = this.routes.indexOf(route);

                    //SI EL COSTO DE IR AL NODO QUE SE ESTA REVISANDO ES MENOR QUE EL MEJOR COSTO ACTUAL
                    //SE VERIFICA QUE SE CUMPLAN LAS RESTRICCIONES DE CAPACIDAD
                    //SI SE CUMPLE TODO SE ACTUALIZA MEJOR VECINO
                    if (cost > worstCost
                            && pickupRestriction(routeIndex, notVisitedNode)
                            && deliveryRestriction(routeIndex, notVisitedNode)
                            && intermediateRestriction(routeIndex, notVisitedNode, route.size() - 2)
                    ) {
                        worstCost = cost;
                        bestNeighbor = notVisitedNode;
                        bestRoute = route;
                        newRouteFlag = false;
                    }
                }

                ArrayList<Integer> newRoute = createNewRoute();
                Integer penultimateNode = newRoute.get(newRoute.size() - 2);
                Integer lastNode = newRoute.get(newRoute.size() - 1);

                float cost = costMatrix.get(penultimateNode).get(notVisitedNode) + costMatrix.get(notVisitedNode).get(lastNode) - costMatrix.get(penultimateNode).get(lastNode);
                if (cost > worstCost) {
                    worstCost = cost;
                    bestNeighbor = notVisitedNode;
                    bestRoute = newRoute;
                    newRouteFlag = true;
                    //AÑADIR RUTA NUEVA A LSITA DE RUTAS
                    //this.routes.add(bestRoute);
                }


            }

            if (newRouteFlag) {
                this.routes.add(bestRoute);
            }

            if (bestRoute == null) {
                return false;
            }
            //SE AÑADE EL MEJOR NODO SIN VISITAR A LA RUTA
            bestRoute.add(bestRoute.size() - 1, bestNeighbor);
            int routeIndex = this.routes.indexOf(bestRoute);
            //SE SUMA EL COSTO DE VISITAR EL NODO
            this.totalCost += Precision.round(worstCost, 2);
            //SE ACTUALIZA EL TOTAL DE PICKUP
            this.updateCurrentPickupQuantities(routeIndex, bestNeighbor);
            //SE ACTUALIZA EL TOTAL DE DELIVERY
            this.updateCurrentDeliveryQuantities(routeIndex, bestNeighbor);
            //SE ACTUALIZA LA CARGA ACTUAL DE CADA RUTA EN CADA NODO
            this.updateCurrentVehiclesLoadNodes(routeIndex, bestNeighbor, bestRoute.size() - 2);


            //SE QUITA ESE NODO DE LA LISTA DE NO VISITADOS
            notVisited.remove(Integer.valueOf(bestNeighbor));
            //SE ANADE ESE NODO A LA LISTA DE VISITADOS
            visited.add(bestNeighbor);
            lastChange = true;
            return true;
        }


        return false;


    }

    public boolean add_farthest_neighbor_first_node() {
        resetLastchanges();
        if (optimal) {
            return false;
        }
        float worstCost = 0.0F;
        Integer bestNeighbor = -1; //SE COMIENZA CON UN VALOR NO VALIDO, PARA LUEGO ACTUALIZARLO CON EL DEL MEJOR VECINO
        ArrayList<Integer> bestRoute = null; //SE GUARDA LA RUTA EN LA QUE SE AGREGA EL MEJOR VECINO
        boolean newRouteFlag = false;

        //SE VERIFICA QUE QUEDEN NODOS POR VISITAR
        if (notVisited.size() > 0) {
            //SE BUSCA EL NODO NO VISITADO QUE AGREGUE EL MENOR COSTO A ALGUNA DE LAS RUTAS DE LA SOLUCION
            for (Integer notVisitedNode : notVisited) {
                for (ArrayList route : this.routes) {
                    //REVISAR ESTA LINEA
                    //SE OBTIENE EL ULTIMO NODO DE LA RUTA
                    Integer firstNode = (Integer) route.get(0);
                    Integer secondNode = (Integer) route.get(1);

                    //SE OBTIENE EL COSTO DE IR DEL ULTIMO NODO DE LA RUTA AL NODO NO VISITADO QUE SE ESTA REVISANDO
                    float cost = costMatrix.get(firstNode).get(notVisitedNode) + costMatrix.get(notVisitedNode).get(secondNode) - costMatrix.get(firstNode).get(secondNode);

                    int routeIndex = this.routes.indexOf(route);

                    //SI EL COSTO DE IR AL NODO QUE SE ESTA REVISANDO ES MENOR QUE EL MEJOR COSTO ACTUAL
                    //SE VERIFICA QUE SE CUMPLAN LAS RESTRICCIONES DE CAPACIDAD
                    //SI SE CUMPLE TODO SE ACTUALIZA MEJOR VECINO
                    if (cost > worstCost
                            && pickupRestriction(routeIndex, notVisitedNode)
                            && deliveryRestriction(routeIndex, notVisitedNode)
                            && intermediateRestriction(routeIndex, notVisitedNode, 1)
                    ) {
                        worstCost = cost;
                        bestNeighbor = notVisitedNode;
                        bestRoute = route;
                        newRouteFlag = false;
                    }
                }

                ArrayList<Integer> newRoute = createNewRoute();
                Integer firstNode = newRoute.get(0);
                Integer secondNode = newRoute.get( 1);

                float cost = costMatrix.get(firstNode).get(notVisitedNode) + costMatrix.get(notVisitedNode).get(secondNode) - costMatrix.get(firstNode).get(secondNode);
                if (cost > worstCost) {
                    worstCost = cost;
                    bestNeighbor = notVisitedNode;
                    bestRoute = newRoute;
                    newRouteFlag = true;
                    //AÑADIR RUTA NUEVA A LSITA DE RUTAS
                    //this.routes.add(bestRoute);
                }


            }

            if (newRouteFlag) {
                this.routes.add(bestRoute);
            }

            if (bestRoute == null) {
                return false;
            }
            //SE AÑADE EL MEJOR NODO SIN VISITAR A LA RUTA
            bestRoute.add(1, bestNeighbor);
            int routeIndex = this.routes.indexOf(bestRoute);
            //SE SUMA EL COSTO DE VISITAR EL NODO
            this.totalCost += Precision.round(worstCost, 2);
            //SE ACTUALIZA EL TOTAL DE PICKUP
            this.updateCurrentPickupQuantities(routeIndex, bestNeighbor);
            //SE ACTUALIZA EL TOTAL DE DELIVERY
            this.updateCurrentDeliveryQuantities(routeIndex, bestNeighbor);
            //SE ACTUALIZA LA CARGA ACTUAL DE CADA RUTA EN CADA NODO
            this.updateCurrentVehiclesLoadNodes(routeIndex, bestNeighbor, 1);


            //SE QUITA ESE NODO DE LA LISTA DE NO VISITADOS
            notVisited.remove(Integer.valueOf(bestNeighbor));
            //SE ANADE ESE NODO A LA LISTA DE VISITADOS
            visited.add(bestNeighbor);
            lastChange = true;
            return true;
        }


        return false;


    }


    public boolean nearest_insertion() {
        resetLastchanges();
        if (optimal) {
            return false;
        }
        float bestCost = Float.MAX_VALUE;
        Integer bestNeighbor = -1; //SE COMIENZA CON UN VALOR NO VALIDO, PARA LUEGO ACTUALIZARLO CON EL DEL MEJOR VECINO
        ArrayList bestRoute = null; //SE GUARDA LA RUTA EN LA QUE SE AGREGA EL MEJOR VECINO
        Integer bestIndex = -1;
        boolean newRouteFlag = false;

        //SE VERIFICA QUE QUEDEN NODOS POR VISITAR
        if (notVisited.size() > 0) {
            for (Integer notVisitedNode : notVisited) {
                for (ArrayList<Integer> route : this.routes) {
                    for (int i = 0; i < route.size() - 1; i++) {
                        float cost = costMatrix.get(route.get(i)).get(notVisitedNode) + costMatrix.get(notVisitedNode).get(route.get(i + 1)) - costMatrix.get(route.get(i)).get(route.get(i + 1));
                        int routeIndex = this.routes.indexOf(route);
                        if (cost < bestCost
                                && pickupRestriction(routeIndex, notVisitedNode)
                                && deliveryRestriction(routeIndex, notVisitedNode)
                                && intermediateRestriction(routeIndex, notVisitedNode, i + 1)
                        ) {
                            bestCost = cost;
                            bestNeighbor = notVisitedNode;
                            bestIndex = i + 1;
                            bestRoute = route;
                            newRouteFlag = false;
                        }
                    }
                }
                ArrayList<Integer> newRoute = createNewRoute();
                Integer penultimateNode = newRoute.get(newRoute.size() - 2);
                Integer lastNode = newRoute.get(newRoute.size() - 1);

                float cost = costMatrix.get(penultimateNode).get(notVisitedNode) + costMatrix.get(notVisitedNode).get(lastNode) - costMatrix.get(penultimateNode).get(lastNode);
                if (cost < bestCost) {
                    bestCost = cost;
                    bestNeighbor = notVisitedNode;
                    bestRoute = newRoute;
                    bestIndex = 1;
                    newRouteFlag = true;
                }

            }

            if (newRouteFlag) {
                this.routes.add(bestRoute);
            }

            if (bestRoute == null) {
                return false;
            }
            bestRoute.add(bestIndex, bestNeighbor);
            int routeIndex = this.routes.indexOf(bestRoute);
            //SE SUMA EL COSTO DE VISITAR EL NODO
            this.totalCost += Precision.round(bestCost, 2);
            //SE ACTUALIZA EL TOTAL DE PICKUP
            this.updateCurrentPickupQuantities(routeIndex, bestNeighbor);
            //SE ACTUALIZA EL TOTAL DE DELIVERY
            this.updateCurrentDeliveryQuantities(routeIndex, bestNeighbor);
            //SE ACTUALIZA LA CARGA ACTUAL DE CADA RUTA EN CADA NODO
            this.updateCurrentVehiclesLoadNodes(routeIndex, bestNeighbor, bestIndex);

            //SE QUITA ESE NODO DE LA LISTA DE NO VISITADOS
            notVisited.remove(Integer.valueOf(bestNeighbor));
            //SE ANADE ESE NODO A LA LISTA DE VISITADOS
            visited.add(bestNeighbor);
            lastChange = true;
            return true;
        }


        return false;
    }
//
//    private int getRandomInteger(int min, int max) {
//        Random random = new Random();
//        return random.ints(min, max)
//                .findFirst()
//                .getAsInt();
//    }
//
//    //Probar
//    public boolean random_move(){
//        resetLastchanges();
//        if(optimal){
//            return false;
//        }
//        if(routes.size()<2){
//            return false;
//        }
//        List<ArrayList<Integer>> routesCopy = getRoutesCopy();
//
//
//        double currentCost = this.totalCost;
//        int fromRouteIndex = getRandomInteger(0,routesCopy.size());
//        int toRouteIndex = getRandomInteger(0,routesCopy.size() + 1);
//        ArrayList<Integer> fromRoute = routesCopy.get(fromRouteIndex);
//
//        int fromPosition = getRandomInteger(1,fromRoute.size() - 1);
//        Integer node = fromRoute.remove(fromPosition);
//        //System.out.println("fromRouteIndex: "+fromRouteIndex + " toRouteIndex: " + toRouteIndex+ " fromPosition: " + fromPosition);
//
//
//
//        if(toRouteIndex == routesCopy.size()){
//            ArrayList<Integer> newRoute = createNewRoute();
//
//            newRoute.add(1,node);
//            routesCopy.add(newRoute);
//
//        }
//        else {
//
//            ArrayList<Integer> toRoute = routesCopy.get(toRouteIndex);
//
//
//            int toPosition = getRandomInteger(1, toRoute.size());
//            toRoute.add(toPosition, node);
//            //System.out.println("node: " + node + " toPosition: " + toPosition);
//        }
//
//        if(fromRoute.size()<=2){
//            routesCopy.remove(fromRouteIndex);
//        }
//
//        List<Float> currentDeliveryQuantities = getCurrentDeliveryQuantitiesFromRoutes(routesCopy);
//        List<Float> currentPickupQuantities = getCurrentPickupQuantitiesFromRoutes(routesCopy);
//        List<ArrayList<Float>> currentLoadNodes = getCurrentLoadNodesFromRoutes(routesCopy);
//        double totalCost = getTotalCostFromRoutes(routesCopy);
//
//        if(deliveryRestriction(currentDeliveryQuantities) && pickupRestriction(currentPickupQuantities) && intermediateRestriction(currentLoadNodes)){
//            this.setCurrentDeliveryQuantities(currentDeliveryQuantities);
//            this.setCurrentPickupQuantities(currentPickupQuantities);
//            this.setCurrentVehiclesLoadNodes(currentLoadNodes);
//            this.setTotalCost(totalCost);
//            this.setRoutes(routesCopy);
//            lastChange = true;
//
//            if(totalCost<currentCost){
//                return true;
//            }
//            else{
//                return false;
//            }
//        }
//
//        return false;
//
//    }

    public boolean farthest_insertion() {
        resetLastchanges();
        if (optimal) {
            return false;
        }
        float worstCost = 0.0F;
        Integer bestNeighbor = -1; //SE COMIENZA CON UN VALOR NO VALIDO, PARA LUEGO ACTUALIZARLO CON EL DEL MEJOR VECINO
        ArrayList bestRoute = null; //SE GUARDA LA RUTA EN LA QUE SE AGREGA EL MEJOR VECINO
        Integer bestIndex = -1;
        boolean newRouteFlag = false;

        //SE VERIFICA QUE QUEDEN NODOS POR VISITAR
        if (notVisited.size() > 0) {
            for (Integer notVisitedNode : notVisited) {
                for (ArrayList<Integer> route : this.routes) {
                    for (int i = 0; i < route.size() - 1; i++) {
                        float cost = costMatrix.get(route.get(i)).get(notVisitedNode) + costMatrix.get(notVisitedNode).get(route.get(i + 1)) - costMatrix.get(route.get(i)).get(route.get(i + 1));
                        int routeIndex = this.routes.indexOf(route);
                        if (cost > worstCost
                                && pickupRestriction(routeIndex, notVisitedNode)
                                && deliveryRestriction(routeIndex, notVisitedNode)
                                && intermediateRestriction(routeIndex, notVisitedNode, i + 1)
                        ) {
                            worstCost = cost;
                            bestNeighbor = notVisitedNode;
                            bestIndex = i + 1;
                            bestRoute = route;
                            newRouteFlag = false;
                        }
                    }
                }
                ArrayList<Integer> newRoute = createNewRoute();
                Integer penultimateNode = newRoute.get(newRoute.size() - 2);
                Integer lastNode = newRoute.get(newRoute.size() - 1);

                float cost = costMatrix.get(penultimateNode).get(notVisitedNode) + costMatrix.get(notVisitedNode).get(lastNode) - costMatrix.get(penultimateNode).get(lastNode);
                if (cost > worstCost) {
                    worstCost = cost;
                    bestNeighbor = notVisitedNode;
                    bestRoute = newRoute;
                    bestIndex = 1;
                    newRouteFlag = true;
                }

            }

            if (newRouteFlag) {
                this.routes.add(bestRoute);
            }

            if (bestRoute == null) {
                return false;
            }
            bestRoute.add(bestIndex, bestNeighbor);
            int routeIndex = this.routes.indexOf(bestRoute);
            //SE SUMA EL COSTO DE VISITAR EL NODO
            this.totalCost += Precision.round(worstCost, 2);
            //SE ACTUALIZA EL TOTAL DE PICKUP
            this.updateCurrentPickupQuantities(routeIndex, bestNeighbor);
            //SE ACTUALIZA EL TOTAL DE DELIVERY
            this.updateCurrentDeliveryQuantities(routeIndex, bestNeighbor);
            //SE ACTUALIZA LA CARGA ACTUAL DE CADA RUTA EN CADA NODO
            this.updateCurrentVehiclesLoadNodes(routeIndex, bestNeighbor, bestIndex);

            //SE QUITA ESE NODO DE LA LISTA DE NO VISITADOS
            notVisited.remove(Integer.valueOf(bestNeighbor));
            //SE ANADE ESE NODO A LA LISTA DE VISITADOS
            visited.add(bestNeighbor);
            lastChange = true;
            return true;
        }


        return false;
    }

    public boolean best_move() {
        resetLastchanges();
        if (optimal) {
            return false;
        }
        if (routes.size() < 2) {
            return false;
        }

        double bestCost = this.totalCost;
        boolean bestCheck = false;
        List<ArrayList<Integer>> routesCopy = getRoutesCopy();
        List<ArrayList<Integer>> routesCopy2 = getRoutesCopy();
        List<ArrayList<Integer>> bestRoutes = getRoutesCopy();

        for (int i = 0; i < routesCopy.size(); i++) {
            ArrayList<Integer> routeFrom = routesCopy2.get(i);

            for (int j = 1; j < routeFrom.size() - 1; j++) {
                routeFrom = routesCopy.get(i);

                Integer node = routeFrom.get(j);
                routeFrom.remove(j);
                if (routeFrom.size() <= 2) {
                    routesCopy2.remove(i);
                }


                for (int k = 0; k < routesCopy2.size() + 1; k++) {

                    List<ArrayList<Integer>> routesCopy3 = getRoutesCopy(routesCopy2);
                    //System.out.printf("i: %d j: %d k: %d routeCopySize: %d\n",i,j,k,routesCopy2.size());

                    ArrayList<Integer> routeTo;
                    if (k == routesCopy2.size()) {
                        routeTo = createNewRoute();
                        routesCopy3.add(routeTo);
                    } else {
                        routeTo = routesCopy3.get(k);
                    }

                    for (int l = 1; l < routeTo.size(); l++) {
                        //System.out.printf("i: %d j: %d k: %d l: %d routeToSize: %d\n",i,j,k,l,routeTo.size());
                        routeTo.add(l, node);

                        List<Float> currentDeliveryQuantities = getCurrentDeliveryQuantitiesFromRoutes(routesCopy3);
                        List<Float> currentPickupQuantities = getCurrentPickupQuantitiesFromRoutes(routesCopy3);
                        List<ArrayList<Float>> currentLoadNodes = getCurrentLoadNodesFromRoutes(routesCopy3);
                        double cost = getTotalCostFromRoutes(routesCopy3);
                        if (cost < bestCost && deliveryRestriction(currentDeliveryQuantities) && pickupRestriction(currentPickupQuantities) && intermediateRestriction(currentLoadNodes)) {
                            bestCost = cost;
                            bestRoutes = getRoutesCopy(routesCopy3);
                            bestCheck = true;
                        }
                        routeTo.remove(l);
                    }
                }
                routesCopy2 = getRoutesCopy();
            }

        }

        List<Float> currentDeliveryQuantities = getCurrentDeliveryQuantitiesFromRoutes(bestRoutes);
        List<Float> currentPickupQuantities = getCurrentPickupQuantitiesFromRoutes(bestRoutes);
        List<ArrayList<Float>> currentLoadNodes = getCurrentLoadNodesFromRoutes(bestRoutes);
        this.setCurrentDeliveryQuantities(currentDeliveryQuantities);
        this.setCurrentPickupQuantities(currentPickupQuantities);
        this.setCurrentVehiclesLoadNodes(currentLoadNodes);
        this.setTotalCost(bestCost);
        this.setRoutes(bestRoutes);
        lastChange = true;

        return bestCheck;
    }

    public boolean worst_move() {
        resetLastchanges();
        if (optimal) {
            return false;
        }
        if (routes.size() < 2) {
            return false;
        }
        double worstCost = this.totalCost;
        boolean bestCheck = false;

        List<ArrayList<Integer>> routesCopy = getRoutesCopy();
        List<ArrayList<Integer>> worstRoutes = getRoutesCopy();

        for (int i = 0; i < routesCopy.size(); i++) {
            ArrayList<Integer> routeFrom = routesCopy.get(i);

            for (int j = 1; j < routeFrom.size() - 1; j++) {
                routeFrom = routesCopy.get(i);
                Integer node = routeFrom.get(j);
                routeFrom.remove(j);
                if (routeFrom.size() <= 2) {
                    routesCopy.remove(i);
                }


                for (int k = 0; k < routesCopy.size() + 1; k++) {

                    List<ArrayList<Integer>> routesCopy2 = getRoutesCopy(routesCopy);
                    //System.out.printf("i: %d j: %d k: %d routeCopySize: %d\n",i,j,k,routesCopy2.size());

                    ArrayList<Integer> routeTo;
                    if (k == routesCopy.size()) {
                        routeTo = createNewRoute();
                        routesCopy2.add(routeTo);
                    } else {
                        routeTo = routesCopy2.get(k);
                    }

                    for (int l = 1; l < routeTo.size(); l++) {
                        //System.out.printf("i: %d j: %d k: %d l: %d routeToSize: %d\n",i,j,k,l,routeTo.size());
                        routeTo.add(l, node);

                        List<Float> currentDeliveryQuantities = getCurrentDeliveryQuantitiesFromRoutes(routesCopy2);
                        List<Float> currentPickupQuantities = getCurrentPickupQuantitiesFromRoutes(routesCopy2);
                        List<ArrayList<Float>> currentLoadNodes = getCurrentLoadNodesFromRoutes(routesCopy2);
                        double cost = getTotalCostFromRoutes(routesCopy2);
                        if (cost > worstCost && deliveryRestriction(currentDeliveryQuantities) && pickupRestriction(currentPickupQuantities) && intermediateRestriction(currentLoadNodes)) {
                            worstCost = cost;
                            worstRoutes = getRoutesCopy(routesCopy2);
                            bestCheck = true;
                        }
                        routeTo.remove(l);
                    }
                }
                routesCopy = getRoutesCopy();
            }

        }

        List<Float> currentDeliveryQuantities = getCurrentDeliveryQuantitiesFromRoutes(worstRoutes);
        List<Float> currentPickupQuantities = getCurrentPickupQuantitiesFromRoutes(worstRoutes);
        List<ArrayList<Float>> currentLoadNodes = getCurrentLoadNodesFromRoutes(worstRoutes);
        this.setCurrentDeliveryQuantities(currentDeliveryQuantities);
        this.setCurrentPickupQuantities(currentPickupQuantities);
        this.setCurrentVehiclesLoadNodes(currentLoadNodes);
        this.setTotalCost(worstCost);
        this.setRoutes(worstRoutes);


        lastChange = true;

        return bestCheck;
    }

    private void resetLastchanges() {
        cplexLastChange = true;
    }

    private boolean deliveryRestriction(List<Float> currentDeliveryQuantities) {
        for (Float deliveryQ : currentDeliveryQuantities) {
            if (deliveryQ > vehicleCapacity) {
                return false;
            }
        }
        return true;
    }

    private boolean pickupRestriction(List<Float> currentPickupQuantities) {
        for (Float pickupQ : currentPickupQuantities) {
            if (pickupQ > vehicleCapacity) {
                return false;
            }
        }
        return true;
    }

    private boolean intermediateRestriction(List<ArrayList<Float>> currentLoadNodes) {
        for (ArrayList<Float> loadNode : currentLoadNodes) {
            for (Float load : loadNode) {
                if (load > vehicleCapacity) {
                    return false;
                }
            }
        }
        return true;
    }

    private double getTotalCostFromRoutes(List<ArrayList<Integer>> routes) {
        double totalCost = 0.0F;
        for (ArrayList<Integer> route : routes) {
            for (int i = 0; i < route.size() - 1; i++) {
                int currentNode = route.get(i);
                int nextNode = route.get(i + 1);
                totalCost += costMatrix.get(currentNode).get(nextNode);
            }
        }

        return Precision.round(totalCost, 2);
    }

    //TERMINAL QUE USA MODELO EXACTO Y CPLEX PARA RESOLVER EL PROBLEMA, USA LA SOLUCION ACTUAL COMO SOLUCION INICIAL, ESTA LIMITADO A 5 SEGUNDOS DE TIEMPO DE COMPUTO
    public boolean cplex_terminal() {
        if (optimal) {
            return false;
        }
        if (!lastChange || !cplexLastChange) {
            return false;
        }

        int n = this.getCostMatrix().size();
        List<ArrayList<Float>> costMatrix = this.getCostMatrix();
        List<Float> d = this.getDeliveryQuantities();
        List<Float> p = this.getPickupQuantities();
        Float Q = this.getVehicleCapacity();

        try {
            IloCplex cplex = new IloCplex();
            cplex.setOut(null);
            //variables
            IloIntVar[][] x = new IloIntVar[n][];
            IloNumVar[][] y = new IloNumVar[n][];
            IloNumVar[][] z = new IloNumVar[n][];

            for (int i = 0; i < n; i++) {
                x[i] = cplex.boolVarArray(n);
                y[i] = cplex.numVarArray(n, 0, Q);
                z[i] = cplex.numVarArray(n, 0, Q);


            }

            //Create objective function and associate with model
            IloLinearNumExpr objective = cplex.linearNumExpr();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    objective.addTerm(costMatrix.get(i).get(j), x[i][j]);
                }
            }

            cplex.addMinimize(objective);

            //constraints
            // sum xij = 1 para cada j
            IloLinearNumExpr[] SumXij = new IloLinearNumExpr[n];

            for (int i = 1; i < n; i++) {
                SumXij[i] = cplex.linearNumExpr();
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        SumXij[i].addTerm(1, x[i][j]);
                    }

                }
                cplex.addEq(SumXij[i], 1);
            }

            //sum xij -sum xji = 0 para cada j en V
            IloLinearNumExpr[] NegativeSumXji = new IloLinearNumExpr[n];
            for (int i = 0; i < n; i++) {
                NegativeSumXji[i] = cplex.linearNumExpr();
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        NegativeSumXji[i].addTerm(-1.0, x[j][i]);
                    }
                }
            }

            for (int i = 0; i < n; i++) {
                cplex.addEq(cplex.sum(cplex.sum(x[i]), NegativeSumXji[i]), 0);
            }

            //sum yij -sum yji = 0 para cada j en C
            IloLinearNumExpr[] NegativeSumYji = new IloLinearNumExpr[n];
            for (int i = 1; i < n; i++) {
                NegativeSumYji[i] = cplex.linearNumExpr();
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        NegativeSumYji[i].addTerm(-1.0, y[j][i]);
                    }

                }
            }

            IloLinearNumExpr[] SumYij = new IloLinearNumExpr[n];
            for (int i = 0; i < n; i++) {
                SumYij[i] = cplex.linearNumExpr();
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        SumYij[i].addTerm(1.0, y[i][j]);
                    }

                }
            }

            for (int i = 1; i < n; i++) {
                cplex.addEq(cplex.sum(SumYij[i], NegativeSumYji[i]), d.get(i - 1));
            }

            //sum zij -sum zji = 0 para cada j en C
            IloLinearNumExpr[] NegativeSumZij = new IloLinearNumExpr[n];
            for (int i = 1; i < n; i++) {
                NegativeSumZij[i] = cplex.linearNumExpr();
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        NegativeSumZij[i].addTerm(-1.0, z[i][j]);
                    }

                }
            }

            IloLinearNumExpr[] SumZji = new IloLinearNumExpr[n];
            for (int i = 0; i < n; i++) {
                SumZji[i] = cplex.linearNumExpr();
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        SumZji[i].addTerm(1.0, z[j][i]);
                    }

                }
            }

            for (int i = 1; i < n; i++) {
                cplex.addEq(cplex.sum(SumZji[i], NegativeSumZij[i]), p.get(i - 1));
            }

            //yij + zij < Qxij para cada i,i i distinto de j

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        cplex.addLe(cplex.sum(y[i][j], z[i][j]), cplex.prod(Q, x[i][j]));
                    }
                }
            }

            double[][] startValues = transformSolutionToCplexFormat();

            IloNumVar[] startVar = new IloIntVar[n * n];
            double[] startVal = new double[n * n];
            for (int i = 0, idx = 0; i < n; ++i)
                for (int j = 0; j < n; ++j) {
                    startVar[idx] = x[i][j];
                    startVal[idx] = startValues[i][j];
                    ++idx;
                }
            cplex.addMIPStart(startVar, startVal, IloCplex.MIPStartEffort.SolveMIP);
            cplex.setParam(IloCplex.Param.ClockType, 1);
            cplex.setParam(IloCplex.Param.TimeLimit, 3);
            cplex.setParam(IloCplex.Param.Simplex.Display, 0);

            cplex.setParam(IloCplex.Param.MIP.Display, 1);
            cplex.setParam(IloCplex.Param.MIP.Limits.Solutions, 2);
            cplex.setParam(IloCplex.Param.Emphasis.MIP, 1);
            cplex.setParam(IloCplex.Param.Threads, 2);


            double start = cplex.getCplexTime();


            if (cplex.solve()) {

                double obj = Precision.round(cplex.getObjValue(), 2);
                this.setTime(cplex.getCplexTime() - start);

                if (obj < this.totalCost || (this.notVisited.size() > 0)) {
                    System.out.println("cplex update:" + obj);
                    this.setTotalCost(obj);
                    double[][] solution = new double[n][n];
                    for (int i = 0; i < n; i++) {
                        for (int j = 0; j < n; j++) {
                            double value = cplex.getValue(x[i][j]);
                            if (value == -0.0) {
                                value = 0.0;
                            }
                            solution[i][j] = value;
                        }
                    }

                    List<ArrayList<Integer>> routes = transformSolutionToPDPFormat(solution);
                    List<Float> currentDeliveryQuantities = getCurrentDeliveryQuantitiesFromRoutes(routes);
                    List<Float> currentPickupQuantities = getCurrentPickupQuantitiesFromRoutes(routes);
                    List<ArrayList<Float>> currentLoadNodeas = getCurrentLoadNodesFromRoutes(routes);
                    this.setRoutes(routes);
                    this.setCurrentDeliveryQuantities(currentDeliveryQuantities);
                    this.setCurrentPickupQuantities(currentPickupQuantities);
                    this.setCurrentVehiclesLoadNodes(currentLoadNodeas);
                    this.setOptimal(cplex.getStatus().toString().equals("Optimal"));
                    List<Integer> visited = new ArrayList<>();
                    for (int i = 0; i < costMatrix.size(); i++) {
                        visited.add(i);
                    }
                    this.setVisited(visited);
                    this.notVisited.clear();
                    if (this.optimal) {
                        System.out.println("Optimal: " + totalCost);
                    }
                    cplex.end();
                    System.out.println("cplex.end");
                    cplexLastChange = true;
                    return true;//devolver solo si la solucion es mejor, y si no era factible y ahora si

                } else {
                    cplex.end();
                    cplexLastChange = false;
                    return false;
                }

            } else {
                System.out.println("problem not solved");

            }

            cplex.end();
            return false;
        } catch (IloException exc) {
            exc.printStackTrace();
            return false;
        }

    }

    //TRANSFORMAR SOLUCION DESDE FORMATO pdp A FORMATO CPLEX
    public double[][] transformSolutionToCplexFormat() {
        int n = this.costMatrix.size();
        double[][] solution = new double[n][n];
        for (ArrayList<Integer> route : this.routes) {
            for (int i = 0; i < route.size() - 1; i++) {
                int currentNode = route.get(i);
                int nextNode = route.get(i + 1);

                solution[currentNode][nextNode] += 1;

            }
        }

        return solution;
    }

    //TRANSFORMAR SOLUCION DESDE FORMATO CPLEX A FORMATO PDP
    public List<ArrayList<Integer>> transformSolutionToPDPFormat(double[][] solution) {
        int n = this.costMatrix.size();
        List<ArrayList<Integer>> routes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (solution[0][i] == 1) {
                ArrayList<Integer> route = new ArrayList<>();
                route.add(0);
                route.add(i);
                int j = i;
                while (j != 0) {
                    boolean end = false;
                    for (int k = 0; k < n && !end; k++) {

                        //System.out.println("i: "+i + " j: "+j+" k: " + k);
                        Long roundedValue = Math.round(solution[j][k]);
                        if (roundedValue == 1) {
                            //System.out.println("add: i: "+i + " j: "+j+" k: " + k);
                            route.add(k);
                            j = k;
                            end = true;
                        }
                    }
                }
                routes.add(route);

            }

        }

        return routes;
    }

    //OBTENER LISTA DE CARGA ENTREGADA DE CADA VEHICULO A PARTIR DE RUTAS
    public List<Float> getCurrentDeliveryQuantitiesFromRoutes(List<ArrayList<Integer>> routes) {
        List<Float> currentDeliveryQuantites = new ArrayList<>();
        for (ArrayList<Integer> route : routes) {
            Float currentDeliveryQuantity = 0.0F;
            for (Integer node : route) {
                if (node != 0) {
                    currentDeliveryQuantity += this.getDeliveryQuantities().get(node - 1);
                }

            }
            currentDeliveryQuantites.add(currentDeliveryQuantity);
        }

        return currentDeliveryQuantites;
    }

    //OBTENER LISTA DE CARGA RECOGIDA DE CADA VEHICULO A PARTIR DE RUTAS
    public List<Float> getCurrentPickupQuantitiesFromRoutes(List<ArrayList<Integer>> routes) {
        List<Float> currentPickupQuantites = new ArrayList<>();
        for (ArrayList<Integer> route : routes) {
            Float currenPickupQuantity = 0.0F;
            for (Integer node : route) {
                if (node != 0) {
                    currenPickupQuantity += this.getPickupQuantities().get(node - 1);
                }
            }
            currentPickupQuantites.add(currenPickupQuantity);
        }

        return currentPickupQuantites;
    }

    //OBTENER LISTA DE CAPACIDADES ACTUAL DE CADA VEHICULO A PARTIR DE RUTAS
    public List<ArrayList<Float>> getCurrentLoadNodesFromRoutes(List<ArrayList<Integer>> routes) {
        List<ArrayList<Float>> currentLoadNodes = new ArrayList<>();
        for (ArrayList<Integer> route : routes) {
            ArrayList<Float> currentLoadNode = new ArrayList<>();

            for (int i = 0; i < route.size(); i++) {
                float loadDelivery = 0.0F;
                float loadPickup = 0.0F;
                //System.out.println("routeSize: " + route.size());
                for (int j = i + 1; j < route.size(); j++) {
                    int node = route.get(j);
                    if (node != 0) {
                        loadDelivery += this.deliveryQuantities.get(node - 1);
                    }
                }


                for (int j = 0; j <= i; j++) {
                    int node = route.get(j);
                    if (node != 0) {
                        loadPickup += this.pickupQuantities.get(node - 1);
                    }
                }
                //System.out.println("add: "+(loadDelivery+loadPickup));
                currentLoadNode.add(loadDelivery + loadPickup);
            }
            currentLoadNodes.add(currentLoadNode);
        }

        return currentLoadNodes;
    }

    public boolean compareRoutes(List<ArrayList<Integer>> routes) {
        if (!(this.routes.size() == routes.size())) {
            return false;
        }


        for (int i = 0; i < this.routes.size(); i++) {
            if (!(this.routes.get(i).equals(routes.get(i)))) {
                return false;
            }
        }
        return true;
    }

    private void printArray(List<?> array) {
        for (Object element : array) {
            System.out.print(element + " ");
        }
        System.out.println();
    }

    public void printNumberOfNodes() {
        System.out.println("Number of nodes: " + this.nNodes);
    }

    public void printCostMatrix() {
        System.out.println("Cost Matrix:");
        for (ArrayList<Float> costList : this.costMatrix) {
            for (Float cost : costList) {
                System.out.printf("%3f ", cost);
            }
            System.out.println();
        }
    }

    public void printDeliveryQuantities() {
        System.out.println("Delivery quantities:");
        this.printArray(this.deliveryQuantities);
    }

    public void printPickupQuantities() {
        System.out.println("Pickup quantitites:");
        this.printArray(this.pickupQuantities);
    }


    public void printRoutes() {
        System.out.println("Routes:");
        for (ArrayList<Integer> route : this.routes) {
            for (Integer cost : route) {
                System.out.print(cost + " ");
            }
            System.out.println();
        }
    }

    public void printTotalCost() {
        System.out.println("Total cost: " + this.totalCost);
    }

    public void printCurrentVehiclesLoadNodes() {
        System.out.println("Current vehicle load nodes:");
        this.printArray(this.currentVehiclesLoadNodes);
    }

    public void printCurrentDeliveryQuantities() {
        System.out.println("Current Delivery Quantities:");
        this.printArray(this.currentDeliveryQuantities);
    }

    public void printCurrentPickupQuantities() {
        System.out.println("Current Pickup Quantities:");
        this.printArray(this.currentPickupQuantities);
    }

    public void printVisited() {
        System.out.println("Visited nodes:");
        this.printArray(this.visited);
    }

    public void printNotVisited() {
        System.out.println("Not Visited nodes:");
        this.printArray(this.notVisited);
    }

    public void printPDPInstance() {
        this.printNumberOfNodes();
        //this.printCostMatrix();
        this.printDeliveryQuantities();
        this.printPickupQuantities();
        this.printCurrentVehiclesLoadNodes();
        this.printCurrentDeliveryQuantities();
        this.printCurrentPickupQuantities();
        this.printRoutes();
        this.printVisited();
        this.printNotVisited();
        this.printTotalCost();


    }

    public boolean isLastChange() {
        return lastChange;
    }

    public void setLastChange(boolean lastChange) {
        this.lastChange = lastChange;
    }

    public boolean isCplexLastChange() {
        return cplexLastChange;
    }

    public void setCplexLastChange(boolean cplexLastChange) {
        this.cplexLastChange = cplexLastChange;
    }
}

/*
    nNodes
    private List<ArrayList<Float>> costMatrix; // MATRIZ DE COSTOS DE LA INSTANCIA
    private List<Float> deliveryQuantities; //LISTA DE CANTIDADES A ENTREGAR DE CADA NODO
    private List<Float> pickupQuantities; //LISTA DE CANTIDADES A RECOGER DE CADA NODO
    private List<Float> vehicleCapacities; //LISTA DE CAPACIDADES DE CADA VEHICULO
    private List<ArrayList<Float>> currentVehiclesLoadNodes; //LISTA DE CAPACIDADES ACTUAL DE CADA VEHICULO
    private List<Float> currentDeliveryQuantities; // LISTA DE CANTIDADES A ENTREGAR ACTUAL DE CADA VEHICULO
    private List<Float> currentPickupQuantities; // LISTA DE CANTIDADES A RECOGER ACTUAL DE CADA VEHICULO
    private List<ArrayList<Integer>>  routes; // (SOLUCION) MATRIZ DE RUTAS, CADA ELEMENTO DE LA LISTA CONTIENE UNA LISTAS QUE REPRESENTA LA RUTA DE CADA VEHICULO
    private List <Integer> visited;
    private List <Integer> notVisited;
    private float totalCost;
 */


/*TODO
- PASAR CONDICIONES DE IF A FUNCIONES (PARA REUTILIZAR EN OTRAS TERMINALES)
- HACER MATRIZ DE CAPACIDAD ACTUAL EN CADA NODO DE LA RUTA (PARA NO SUPERAR CARGA EN NINGUN PUNTO)

 */