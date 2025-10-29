export class User{
    id?: string
    name: string
    password: string
    email: string
    nick?: string
    birth?: Date
    age?: number
    photo?: string
    phone?: string 

    constructor(    
    id: string,
    name: string,
    password: string,
    email: string,
    nick: string,
    birth: Date,
    age: number,
    photo: string,
    phone: string
)
    {
        this.id=id;
        this.name=name;
        this.password=password;
        this.email=email;
        this.nick=nick;
        this.birth=birth;
        this.age=age;
        this.photo=photo;
        this.phone=phone;
    }


}