from enum import Enum

class QueryComponent(Enum):
	Min = "min"
	Max = "max"

class Node:
	name = None
	region = None
	neightbors = None
	points = None

	def __init__(self, name, region):
		self.name = name
		self.region = region
		self.neightbors = set()
		self.points = set()

	def __str__(self):
		return "node(%s, %s, %s, %s)" % \
			(self.name, self.region, [n.name for n in self.neightbors], self.points)

	def add(self, *neightbors):
		for neightbor in neightbors:
			self.neightbors.add(neightbor)

	def add_points(self, *points):
		for point in points:
			self.points.add(point)

	def is_sq_starter(self, q):
		for i in range(0, len(q.dims)):
			if q.dims[i] == QueryComponent.Min and self.region.dims[i].low > 0:
				return False
			if q.dims[i] == QueryComponent.Max and self.region.dims[i].high < Range.MAX:
				return False
		return True

	def find_nearer_node(self, q):
		for m in routing_table(self):
			for i in range(0, len(q.dims)):
				if q.dims[i] == QueryComponent.Min and m.region.dims[i].low > self.region.dims[i].low:
					break
				if q.dims[i] == QueryComponent.Max and m.region.dims[i].high < self.region.dims[i].high:
					break
			else:
				return m
		print("No nearer node!")
		return None

	def is_in_charge_of(self, sr):
		if len(sr.regions) == 0:
		#if len(intersect(n.region, sr).regions) == 0:
			return False
		return True

class Query:
	dims = None

	def __init__(self, *dims):
		self.dims = dims

	def __str__(self):
		return "query%s" % ([n.value for n in self.dims])

	def is_skyline_point(self, point, points):
		for p in points:
			if point is not p and self.dominates(p, point):
				return False
		return True

	def dominates(self, p1, p2):
		for i in range(0, len(self.dims)):
			if self.dims[i] == QueryComponent.Min and p1[i] > p2[i]:
				return False
			if self.dims[i] == QueryComponent.Max and p1[i] < p2[i]:
				return False
		return True

class Range():
	MAX = 9

	low = None
	high = None

	def __init__(self, low, high):
		self.low = low
		self.high = high

	def __str__(self):
		return "[%s,%s]" % (self.low, self.high)

	def __eq__(self, other):
		return self.low == other.low and \
			self.high == other.high

	def valid(self):
		return self.low <= self.high

class Region():
	dims = None

	def __init__(self, *dimensions):
		self.dims = [dim for dim in dimensions]

	def __str__(self):
		return "r(%s)" % (", ".join(str(r) for r in self.dims))

	def __eq__(self, other):
		return self.dims == other.dims

	def valid(self):
		return all(dim.valid() for dim in self.dims)

	def intersect(self, r2):
		dims = [Range(0, 0), Range(0, 0)]

		for i in range(0, len(self.dims)):
			dims[i].low = max(self.dims[i].low, r2.dims[i].low)
			dims[i].high = min(self.dims[i].high, r2.dims[i].high)

			if dims[i].low > dims[i].high:
				return None

		return Region(*dims)


	def subtract(self, r2):
		regions = []

		def add_region_if_valid(*dims):
			for i in range(0, len(dims)):
				if dims[i].low < self.dims[i].low:
					return
				if dims[i].high > self.dims[i].high:
					return
			r = Region(*dims)
			if r.valid() and r not in regions:
				regions.append(r)

		for j in range(0, len(self.dims)):
			dims_low = []
			dims_high = []
			for i in range(0, len(self.dims)):
				if i == j:
					dim_low = Range(self.dims[i].low, r2.dims[i].low-1)
					dim_high = Range(r2.dims[i].high+1, self.dims[i].high)
				else:
					dim_low = Range(self.dims[i].low, self.dims[i].high)
					dim_high = Range(self.dims[i].low, self.dims[i].high)
				dims_low.append(dim_low)
				dims_high.append(dim_high)

			add_region_if_valid(*dims_low)
			add_region_if_valid(*dims_high)

		return regions

	def covers(self, r2):
		for i in range(0, len(self.dims)):
			if self.dims[i].low > r2.dims[i].low:
				return False
			if self.dims[i].high < r2.dims[i].high:
				return False
		return True


class SearchRegion:
	regions = None

	def __init__(self, *regions):
		self.regions = [r for r in regions]

	def __str__(self):
		return "sr(%s)" % (", ".join([str(r) for r in self.regions]))

	def disjoint(self):
		sr = self
		regions = []
		while len(sr.regions) > 0:
			region = sr.regions[0]
			sr = sr.subtract(region)
			if region not in regions:
				regions.append(region)
		return SearchRegion(*regions)

	def union(self, sr2):
		regions = []
		for region in self.regions:
			regions.append(region)
		for region in sr2.regions:
			regions.append(region)
		return SearchRegion(*regions)

	def intersect(self, r):
		regions = []
		for search_region in self.regions:
			inter = search_region.intersect(r)
			if inter is not None:
				regions.append(inter)
		return SearchRegion(*regions)

	def subtract(self, r):
		regions = []
		for search_region in self.regions:
			inter = search_region.intersect(r)
			if inter is not None:
				sub = search_region.subtract(r)
				for region in sub:
					if region not in regions:
						regions.append(region)
			else:
				if search_region not in regions:
					regions.append(search_region)
		return SearchRegion(*regions)

	def is_covered_by(self, r):
		for search_region in self.regions:
			if not r.covers(search_region):
				return False
		return True


def greedy_skyline_search(n, q, sr, p):
	print("Running GSS (phase %d) on %s" % (p, n))
	#print("Search region: %s" % sr)
	if n is None:
		print("Node is None!")
		return

	if p == 1:
		if n.is_sq_starter(q):
			local_skyline_points = compute_skyline_points(n, q)
			p_md = compute_pmd(local_skyline_points, q)
			print("pmd: %s" % str(p_md))
			SR = compute_search_region(p_md, q)
			# Partition SR into a disjoint set of subSRs for neighbornodes in RT(n)
			partitions, _ = partition(SR, n)
			#for key, value in partitions.items():
			#	print("%s \n   -> %s" % (key, value))
			for m in routing_table(n):
				subSR = partitions[m]
				if m.is_in_charge_of(subSR):
					for point in greedy_skyline_search(m, q, subSR, 2):
						yield point
			# return local skyline points
			for point in local_skyline_points:
				yield point
		else:
			x = n.find_nearer_node(q)
			for point in greedy_skyline_search(x, q, None, 1):
				yield point
	elif p == 2:
		local_skyline_points = compute_skyline_points(n, q)
		# return local skyline points
		for point in local_skyline_points:
			yield point
		if not sr.is_covered_by(n.region):
			SR = sr.subtract(n.region)
			partitions, _ = partition(SR, n)
			#for key, value in partitions.items():
			#	print("%s \n   -> %s" % (key, value))
			for m in routing_table(n):
				subSR = partitions[m]
				if m.is_in_charge_of(subSR):
					for point in greedy_skyline_search(m, q, subSR, 2):
						yield point

def routing_table(n):
	return n.neightbors

def partition(sr, n):
	partitions = dict()
	# give a sub search region to the current node
	partitions[n] = sr.intersect(n.region)
	sr = sr.subtract(n.region)
	# give a sub search region to each of its neighbors
	for m in routing_table(n):
		partitions[m] = sr.intersect(m.region)
		sr = sr.subtract(m.region)
	# merge the unallocated of the search region with suitable neighbors
	for m in routing_table(n):
		if len(partitions[m].regions) > 0:
			# find a neighbor of node m that covers a bit of the unallocated search region
			for o in routing_table(m):
				intersection = sr.intersect(o.region)
				if len(intersection.regions) > 0:
					parts, sr = partition(sr, o)
					for region in parts.values():
						partitions[m] = partitions[m].union(region)
	return partitions, sr


def compute_skyline_points(n, q):
	print("Computing skyline points in %s" % n.name)
	return {p for p in n.points if q.is_skyline_point(p, n.points)}

def compute_pmd(points, q):
	if len(points) == 0:
		print("Warning: no p_md!")

	max_score = -1
	max_point = None
	for p in points:
		dominating = compute_dominating_region(p, q)
		score = sum(dominating.dims[i].high-dominating.dims[i].low \
			for i in range(0, len(q.dims)))
		if score > max_score:
			max_score = score
			max_point = p
	return max_point

def compute_dominating_region(p, q):
	dimensions = []
	for i in range(0, len(q.dims)):
		if q.dims[i] == QueryComponent.Min:
			dimensions.append(Range(p[i]+1, Range.MAX))
		if q.dims[i] == QueryComponent.Max:
			dimensions.append(Range(0, p[i]))
	return Region(*dimensions)

def compute_search_region(pmd, q):
	full = SearchRegion(Region(*(Range(0, Range.MAX) for _ in q.dims)))

	if pmd is None:
		return full

	search_region = full.subtract(compute_dominating_region(pmd, q))
	return search_region


def find_node(n, p):
	#print(n, p)
	if all(p[i] >= n.region.dims[i].low and p[i] <= n.region.dims[i].high \
		for i in range(0, len(n.region.dims))):
		return n
	#for i in range(0, len(n.region.dims)):
	#	if p[i] < n.region.dims[i].low or p[i] > n.region.dims[i].high:
	#		break
	#else:
	#	return n

	for m in routing_table(n):
		for i in range(0, len(m.region.dims)):
			if p[i] < n.region.dims[i].low and m.region.dims[i].low > n.region.dims[i].low:
				break
			if p[i] > n.region.dims[i].high and m.region.dims[i].high < n.region.dims[i].high:
				break
			if p[i] >= n.region.dims[i].low and p[i] <= n.region.dims[i].high and \
				(p[i] < m.region.dims[i].low or p[i] > m.region.dims[i].high):
				break
		else:
			return find_node(m, p)
	return None


# ---

def init():
	# AAABBBCCCC
	# AAA.BBCC.C
	# ,AABBBC.CC
	# AAAB.BCCCC
	# A,ADDDE.FF
	# AAAD.DEEF.
	# AAA.DDEEGG
	# HHHH.HEEG.
	# HH,HHHEEGG
	# HHHHHH,E,G

	a = Node("A", Region(Range(0, 2), Range(3, 9)))
	b = Node("B", Region(Range(3, 5), Range(6, 9)))
	c = Node("C", Region(Range(6, 9), Range(6, 9)))
	d = Node("D", Region(Range(3, 5), Range(3, 5)))
	e = Node("E", Region(Range(6, 7), Range(0, 5)))
	f = Node("F", Region(Range(8, 9), Range(4, 5)))
	g = Node("G", Region(Range(8, 9), Range(0, 3)))
	h = Node("H", Region(Range(0, 5), Range(0, 2)))

	a.add(b, d, h)
	b.add(a, c, d)
	c.add(b, e, f)
	d.add(a, b, e, h)
	e.add(c, d, f, g, h)
	f.add(c, e, g)
	g.add(e, f)
	h.add(a, d, e)

	p = [(1, 5), (0, 7), (3, 8), (4, 6), (8, 8), (7, 7), (3, 3), (4, 4), \
			(6, 0), (7, 5), (9, 4), (9, 2), (8, 0), (2, 1), (4, 2)]

	p = [(9-point[0], 9-point[1]) for point in p]

	import random
	p = [(random.randint(0,9),random.randint(0,9)) for point in p]

	for point in p:
		node = find_node(h, point)
		node.add_points(point)

	print(a)
	print(b)
	print(c)
	print(d)
	print(e)
	print(f)
	print(g)

	return b, p

def main():
	n, p = init()

	q = Query(QueryComponent.Min, QueryComponent.Min)
	print(q)

	skyline = set()
	for point in greedy_skyline_search(n, q, None, 1):
		print(point)
		skyline.add(point)

	pruned = {p for p in skyline if q.is_skyline_point(p, skyline)}

	print("Skyline points: %s" % skyline)
	print("Pruned skyline points: %s" % pruned)

	# print nodes+points
	colored = False
	for y in range(Range.MAX, -1, -1):
		line = "|"
		for x in range(0, 10):
			node = find_node(n, (x, y))
			right = find_node(n, (x+1, y))
			bottom = find_node(n, (x, y-1))
			l = node.name#.lower()
			v = "|" if node != right and right is not None else " "
			if colored:
				h = "\033[4m" if node != bottom and bottom is not None else ""
				if (x, y) in pruned:
					line += h+"[\033[92m"+l+"\033[0m"+h+"]"
				elif (x, y) in skyline:
					line += h+" \033[94m"+l+"\033[0m"+h+v
				elif (x, y) in p:
					line += h+" \033[1m"+l+"\033[0m"+h+v
				else:
					line += h+" "+l+v
				line += "\033[0m"
			else:
				if (x, y) in pruned:
					line += "[o]"
				elif (x, y) in skyline:
					line += "(*)"
				elif (x, y) in p:
					line += " ."+v
				else:
					line += " "+l+v
		line += "|"
		print(line)

main()
