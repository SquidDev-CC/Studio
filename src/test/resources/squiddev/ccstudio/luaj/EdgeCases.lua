-- Edge case when a parameter assignment occurs after a non-returning branch statement in a VarArgsFunction subclass

local function arg(a, b, ...)
	if a then error() end
	b = b + 1
end
