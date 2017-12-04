var svg;
var triples;
var force;
var graph;

/**
 * Function that using ajax to response N-Triples from server and create graph
 */
function createGraph() {
    $.ajax({
        type: 'POST',
        url: window.location.href + "/getNTriples",
        dataType: "html",
        success: function (response) {

            if(response.localeCompare("0") == 0){
                console.log("ERROR - IOException")
            }
            if(response.localeCompare("1") == 0){
                console.log("ERROR - fileID not exists")
            }
            else{
                svg = d3.select("#svg-body").append("svg")
                    .attr("width", 1300)
                    .attr("height", 768)
                ;

                triples = JSON.parse(response);

                force = d3.layout.force().size([1300, 768]);

                graph = triplesToGraph(triples, true);

                update();
            }

        }
    });


}

/**
 * To split object nodes with same value to different nodes
 */
function splitObjects(){
    graph = triplesToGraph(triples, false);
    $('#objectMerge').attr('onclick', 'mergeObjects()');
    $('#objectMerge').attr('value', 'Merge objects');
    update();
}

/**
 * To merge object nodes with same value to one node
 */
function mergeObjects(){
    graph = triplesToGraph(triples, true);
    $('#objectMerge').attr('onclick', 'splitObjects()');
    $('#objectMerge').attr('value', 'Split objects');
    update();
}

function filterNodesById(nodes,id){
    return nodes.filter(function(n) { return n.id === id; });
}

/**
 * Process N-Triples to Graph
 * @param triples
 * @param objectsMerge contains true or false (true - merge same object nodes, false - split same object nodes)
 * @returns {{nodes: Array, links: Array}}
 */
function triplesToGraph(triples, objectsMerge){

    svg.html("");
    //Graph
    var graph={nodes:[], links:[]};

    //Initial Graph from triples
    triples.forEach(function(triple){
        var subjId = triple.subject;
        var predId = triple.predicate;
        var objId = triple.object;

        var subjNode = filterNodesById(graph.nodes, subjId)[0];

        var objNode = null;

        if(objectsMerge === true){
            objNode = filterNodesById(graph.nodes, objId)[0];
        }

        if(subjNode==null){
            subjNode = {id:subjId, label:subjId, weight:1};
            graph.nodes.push(subjNode);
        }

        if(objNode==null){
            objNode = {id:objId, label:objId, weight:1};
            graph.nodes.push(objNode);
        }

        graph.links.push({source:subjNode, target:objNode, predicate:predId, weight:1});
    });

    if(objectsMerge === true) {
        //To merge predicates that were point at same object (predicates called in graph as merge:predicate)
        var newGraph = {nodes: [], links: []};
        for (var i = 0; i < graph.links.length; i++) {
            var added = false;
            for (var j = 0; j < graph.links.length; j++) {
                if (i !== j) {
                    if (graph.links[i].source === graph.links[j].source && graph.links[i].target === graph.links[j].target) {
                        newGraph.links.push({
                            source: graph.links[i].source,
                            target: graph.links[i].target,
                            predicate: "merged:predicates",
                            weight: 1
                        });
                        added = true;
                    }
                }
            }
            if (added === false) {
                newGraph.links.push({
                    source: graph.links[i].source,
                    target: graph.links[i].target,
                    predicate: graph.links[i].predicate,
                    weight: 1
                });
            }
        }
        newGraph.nodes = graph.nodes;
        graph = newGraph;
    }

    return graph;
}

/**
 * Update graph elements
 */
function update(){
    // ==================== Add Marker ====================
    svg.append("svg:defs").selectAll("marker")
        .data(["end"])
        .enter().append("svg:marker")
        .attr("id", String)
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", 30)
        .attr("refY", -0.5)
        .attr("markerWidth", 6)
        .attr("markerHeight", 6)
        .attr("orient", "auto")
        .append("svg:polyline")
        .attr("points", "0,-5 10,0 0,5")
    ;

    // ==================== Add Links ====================
    var links = svg.selectAll(".link")
        .data(graph.links)
        .enter()
        .append("line")
        .attr("marker-end", "url(#end)")
        .attr("class", "link")
        .attr("stroke-width",1)
    ;//links

    // ==================== Add Link Names =====================
    var linkTexts = svg.selectAll(".link-text")
        .data(graph.links)
        .enter()
        .append("text")
        .attr("class", "link-text")
        .text( function (d) { return d.predicate; })
    ;

    // ==================== Add Link Names =====================
    var nodeTexts = svg.selectAll(".node-text")
        .data(graph.nodes)
        .enter()
        .append("text")
        .attr("class", "node-text")
        .text( function (d) { return d.label; })
    ;

    // ==================== Add Node =====================
    var nodes = svg.selectAll(".node")
        .data(graph.nodes)
        .enter()
        .append("circle")
        .attr("class", "node")
        .attr("r",8)
        .call(force.drag)
    ;//nodes

    // ==================== Force ====================
    force.on("tick", function() {
        nodes
            .attr("cx", function(d){ return d.x; })
            .attr("cy", function(d){ return d.y; })
        ;

        links
            .attr("x1", 	function(d)	{ return d.source.x; })
            .attr("y1", 	function(d) { return d.source.y; })
            .attr("x2", 	function(d) { return d.target.x; })
            .attr("y2", 	function(d) { return d.target.y; })
        ;

        nodeTexts
            .attr("x", function(d) { return d.x + 12 ; })
            .attr("y", function(d) { return d.y + 3; })
        ;

        linkTexts
            .attr("x", function(d) { return 4 + (d.source.x + d.target.x)/2  ; })
            .attr("y", function(d) { return 4 + (d.source.y + d.target.y)/2 ; })
        ;
    });

    // ==================== Run ====================
    force
        .nodes(graph.nodes)
        .links(graph.links)
        .charge(-500)
        .linkDistance(100)
        .start()
    ;
}