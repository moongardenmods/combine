--- @meta -

--- @class byte:integer \-127 to 128
--- @class short:integer \-32,768 to 32767
--- @class int:integer \-2^31 to 2^31-1 OR 0 to 2^32-1
--- @class long:integer \-2^63 to 2^63-1 OR 0 to 2^64-1
--- @class float:number Single-precision 32-bit IEEE 754 floating point
--- @class double:number Double-precision 64-bit IEEE 754 floating point.
--- @class char:string A string with a single character.

---@class ClassUserdata:userdata A userdata representing a Java class.
local ClassUserdata = {}

---@class InstanceUserdata:userdata A userdata representing a Java object.
local InstanceUserdata = {}